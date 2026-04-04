import { Injectable, inject } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { map, Observable } from 'rxjs';

import { PageResponse } from '../../../models/common/page-response.model';
import { UserResponse, CreateUserRequest, UpdateUserRequest, UsersQueryParams } from './users.dto';

const USERS_QUERY = gql`
  query GetUsers($filter: UserFilterInput) {
    users(filter: $filter) {
      content { id username role }
      page size totalElements totalPages first last sortBy sortDirection
    }
  }
`;

const USER_QUERY = gql`
  query GetUser($id: Long!) {
    user(id: $id) {
      id username role
    }
  }
`;

const CREATE_USER_MUTATION = gql`
  mutation CreateUser($input: CreateUserInput!) {
    createUser(input: $input) {
      id username role
    }
  }
`;

const UPDATE_USER_MUTATION = gql`
  mutation UpdateUser($id: Long!, $input: UpdateUserInput!) {
    updateUser(id: $id, input: $input) {
      id username role
    }
  }
`;

const DELETE_USER_MUTATION = gql`
  mutation DeleteUser($id: Long!) {
    deleteUser(id: $id)
  }
`;

@Injectable({ providedIn: 'root' })
export class UsersGraphqlService {
  private readonly apollo = inject(Apollo);

  findAll(query: UsersQueryParams = {}): Observable<PageResponse<UserResponse>> {
    const filter: Record<string, unknown> = {};
    if (query.username) filter['username'] = query.username;
    if (query.role) filter['role'] = query.role;
    filter['page'] = query.page ?? 0;
    filter['size'] = query.size ?? 10;
    filter['sortBy'] = query.sortBy ?? 'username';
    filter['sortDir'] = query.sortDir ?? 'asc';

    return this.apollo.query<{ users: PageResponse<UserResponse> }>({
      query: USERS_QUERY,
      variables: { filter },
    }).pipe(map(result => result.data!.users));
  }

  findById(userId: number): Observable<UserResponse> {
    return this.apollo.query<{ user: UserResponse }>({
      query: USER_QUERY,
      variables: { id: userId },
    }).pipe(map(result => result.data!.user));
  }

  create(payload: CreateUserRequest): Observable<UserResponse> {
    return this.apollo.mutate<{ createUser: UserResponse }>({
      mutation: CREATE_USER_MUTATION,
      variables: { input: payload },
    }).pipe(map(result => result.data!.createUser));
  }

  update(userId: number, payload: UpdateUserRequest): Observable<UserResponse> {
    return this.apollo.mutate<{ updateUser: UserResponse }>({
      mutation: UPDATE_USER_MUTATION,
      variables: { id: userId, input: payload },
    }).pipe(map(result => result.data!.updateUser));
  }

  delete(userId: number): Observable<boolean> {
    return this.apollo.mutate<{ deleteUser: boolean }>({
      mutation: DELETE_USER_MUTATION,
      variables: { id: userId },
    }).pipe(map(result => result.data!.deleteUser));
  }
}
