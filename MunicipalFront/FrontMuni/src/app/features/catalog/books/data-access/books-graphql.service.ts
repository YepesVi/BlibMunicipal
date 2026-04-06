import { Injectable, inject } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { map, Observable } from 'rxjs';

import { PageResponse } from '../../../../models/common/page-response.model';
import {
  BookResponse,
  BookSummaryResponse,
  BooksQueryParams,
  CreateBookRequest,
  UpdateBookRequest,
  AttachBookImagesRequest,
} from './books.dto';

const BOOKS_QUERY = gql`
  query GetBooks($filter: BookFilterInput) {
    books(filter: $filter) {
      content {
        id isbn title publisher publicationYear
        authorId authorName categoryId categoryName
        primaryImageUrl createdAt updatedAt
      }
      page size totalElements totalPages first last sortBy sortDirection
    }
  }
`;

const BOOK_DETAIL_QUERY = gql`
  query GetBook($id: Long!) {
    book(id: $id) {
      id isbn title publisher publicationYear description
      authorId authorName categoryId categoryName primaryImageUrl
      images { id mediaAssetId secureUrl primaryImage sortOrder altText }
      createdAt updatedAt
    }
  }
`;

const BOOK_BY_ISBN_QUERY = gql`
  query GetBookByIsbn($isbn: String!) {
    bookByIsbn(isbn: $isbn) {
      id isbn title publisher publicationYear description
      authorId authorName categoryId categoryName primaryImageUrl
      images { id mediaAssetId secureUrl primaryImage sortOrder altText }
      createdAt updatedAt
    }
  }
`;

const CREATE_BOOK_MUTATION = gql`
  mutation CreateBook($input: CreateBookInput!) {
    createBook(input: $input) {
      id isbn title publisher publicationYear description
      authorId authorName categoryId categoryName primaryImageUrl
      images { id mediaAssetId secureUrl primaryImage sortOrder altText }
      createdAt updatedAt
    }
  }
`;

const UPDATE_BOOK_MUTATION = gql`
  mutation UpdateBook($id: Long!, $input: UpdateBookInput!) {
    updateBook(id: $id, input: $input) {
      id isbn title publisher publicationYear description
      authorId authorName categoryId categoryName primaryImageUrl
      images { id mediaAssetId secureUrl primaryImage sortOrder altText }
      createdAt updatedAt
    }
  }
`;

const DELETE_BOOK_MUTATION = gql`
  mutation DeleteBook($id: Long!) {
    deleteBook(id: $id)
  }
`;

const ATTACH_IMAGES_MUTATION = gql`
  mutation AttachBookImages($bookId: Long!, $input: AttachImagesInput!) {
    attachBookImages(bookId: $bookId, input: $input) {
      id images { id mediaAssetId secureUrl primaryImage sortOrder altText }
    }
  }
`;

const SET_PRIMARY_IMAGE_MUTATION = gql`
  mutation SetPrimaryBookImage($bookId: Long!, $bookImageId: Long!) {
    setPrimaryBookImage(bookId: $bookId, bookImageId: $bookImageId) {
      id images { id mediaAssetId secureUrl primaryImage sortOrder altText }
    }
  }
`;

const REMOVE_IMAGE_MUTATION = gql`
  mutation RemoveBookImage($bookId: Long!, $bookImageId: Long!) {
    removeBookImage(bookId: $bookId, bookImageId: $bookImageId)
  }
`;

@Injectable({ providedIn: 'root' })
export class BooksGraphqlService {
  private readonly apollo = inject(Apollo);

  findAll(query: BooksQueryParams = {}): Observable<PageResponse<BookSummaryResponse>> {
    const filter: Record<string, unknown> = {};
    if (query.title) filter['title'] = query.title;
    if (query.authorId) filter['authorId'] = query.authorId;
    if (query.categoryId) filter['categoryId'] = query.categoryId;
    if (query.publicationYear) filter['publicationYear'] = query.publicationYear;
    filter['page'] = query.page ?? 0;
    filter['size'] = query.size ?? 10;
    filter['sortBy'] = query.sortBy ?? 'title';
    filter['sortDir'] = query.sortDir ?? 'asc';

    return this.apollo.query<{ books: PageResponse<BookSummaryResponse> }>({
      query: BOOKS_QUERY,
      variables: { filter },
    }).pipe(map(result => result.data!.books));
  }

  findById(bookId: number): Observable<BookResponse> {
    return this.apollo.query<{ book: BookResponse }>({
      query: BOOK_DETAIL_QUERY,
      variables: { id: bookId },
    }).pipe(map(result => result.data!.book));
  }

  findByIsbn(isbn: string): Observable<BookResponse> {
    return this.apollo.query<{ bookByIsbn: BookResponse }>({
      query: BOOK_BY_ISBN_QUERY,
      variables: { isbn },
    }).pipe(map(result => result.data!.bookByIsbn));
  }

  create(payload: CreateBookRequest): Observable<BookResponse> {
    return this.apollo.mutate<{ createBook: BookResponse }>({
      mutation: CREATE_BOOK_MUTATION,
      variables: { input: payload },
    }).pipe(map(result => result.data!.createBook));
  }

  update(bookId: number, payload: UpdateBookRequest): Observable<BookResponse> {
    return this.apollo.mutate<{ updateBook: BookResponse }>({
      mutation: UPDATE_BOOK_MUTATION,
      variables: { id: bookId, input: payload },
    }).pipe(map(result => result.data!.updateBook));
  }

  delete(bookId: number): Observable<boolean> {
    return this.apollo.mutate<{ deleteBook: boolean }>({
      mutation: DELETE_BOOK_MUTATION,
      variables: { id: bookId },
    }).pipe(map(result => result.data!.deleteBook));
  }

  attachImages(bookId: number, payload: AttachBookImagesRequest): Observable<BookResponse> {
    return this.apollo.mutate<{ attachBookImages: BookResponse }>({
      mutation: ATTACH_IMAGES_MUTATION,
      variables: { bookId, input: payload },
    }).pipe(map(result => result.data!.attachBookImages));
  }

  setPrimaryImage(bookId: number, bookImageId: number): Observable<BookResponse> {
    return this.apollo.mutate<{ setPrimaryBookImage: BookResponse }>({
      mutation: SET_PRIMARY_IMAGE_MUTATION,
      variables: { bookId, bookImageId },
    }).pipe(map(result => result.data!.setPrimaryBookImage));
  }

  removeImage(bookId: number, bookImageId: number): Observable<boolean> {
    return this.apollo.mutate<{ removeBookImage: boolean }>({
      mutation: REMOVE_IMAGE_MUTATION,
      variables: { bookId, bookImageId },
    }).pipe(map(result => result.data!.removeBookImage));
  }
}
