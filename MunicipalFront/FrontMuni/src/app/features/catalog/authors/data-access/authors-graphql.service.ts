import { Injectable, inject } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { map, Observable } from 'rxjs';

import { AuthorResponse, CreateAuthorRequest, UpdateAuthorRequest } from './authors.dto';

const AUTHORS_QUERY = gql`
  query GetAuthors($fullName: String) {
    authors(fullName: $fullName) {
      id idCard fullName nationality biography createdAt updatedAt
    }
  }
`;

const AUTHOR_QUERY = gql`
  query GetAuthor($id: Long!) {
    author(id: $id) {
      id idCard fullName nationality biography createdAt updatedAt
    }
  }
`;

const AUTHOR_BY_ID_CARD_QUERY = gql`
  query GetAuthorByIdCard($idCard: String!) {
    authorByIdCard(idCard: $idCard) {
      id idCard fullName nationality biography createdAt updatedAt
    }
  }
`;

const CREATE_AUTHOR_MUTATION = gql`
  mutation CreateAuthor($input: CreateAuthorInput!) {
    createAuthor(input: $input) {
      id idCard fullName nationality biography createdAt updatedAt
    }
  }
`;

const UPDATE_AUTHOR_MUTATION = gql`
  mutation UpdateAuthor($id: Long!, $input: UpdateAuthorInput!) {
    updateAuthor(id: $id, input: $input) {
      id idCard fullName nationality biography createdAt updatedAt
    }
  }
`;

const DELETE_AUTHOR_MUTATION = gql`
  mutation DeleteAuthor($id: Long!) {
    deleteAuthor(id: $id)
  }
`;

@Injectable({ providedIn: 'root' })
export class AuthorsGraphqlService {
  private readonly apollo = inject(Apollo);

  findAll(fullName?: string): Observable<AuthorResponse[]> {
    return this.apollo.query<{ authors: AuthorResponse[] }>({
      query: AUTHORS_QUERY,
      variables: fullName ? { fullName } : {},
    }).pipe(map(result => result.data!.authors));
  }

  findById(authorId: number): Observable<AuthorResponse> {
    return this.apollo.query<{ author: AuthorResponse }>({
      query: AUTHOR_QUERY,
      variables: { id: authorId },
    }).pipe(map(result => result.data!.author));
  }

  findByIdCard(idCard: string): Observable<AuthorResponse> {
    return this.apollo.query<{ authorByIdCard: AuthorResponse }>({
      query: AUTHOR_BY_ID_CARD_QUERY,
      variables: { idCard },
    }).pipe(map(result => result.data!.authorByIdCard));
  }

  create(payload: CreateAuthorRequest): Observable<AuthorResponse> {
    return this.apollo.mutate<{ createAuthor: AuthorResponse }>({
      mutation: CREATE_AUTHOR_MUTATION,
      variables: { input: payload },
    }).pipe(map(result => result.data!.createAuthor));
  }

  update(authorId: number, payload: UpdateAuthorRequest): Observable<AuthorResponse> {
    return this.apollo.mutate<{ updateAuthor: AuthorResponse }>({
      mutation: UPDATE_AUTHOR_MUTATION,
      variables: { id: authorId, input: payload },
    }).pipe(map(result => result.data!.updateAuthor));
  }

  delete(authorId: number): Observable<boolean> {
    return this.apollo.mutate<{ deleteAuthor: boolean }>({
      mutation: DELETE_AUTHOR_MUTATION,
      variables: { id: authorId },
    }).pipe(map(result => result.data!.deleteAuthor));
  }
}
