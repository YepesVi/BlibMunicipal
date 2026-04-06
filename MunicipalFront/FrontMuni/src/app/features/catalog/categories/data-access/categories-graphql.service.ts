import { Injectable, inject } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { map, Observable } from 'rxjs';

import {
  CategoryResponse,
  CategoryTreeResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
} from './categories.dto';

const CATEGORIES_QUERY = gql`
  query GetCategories {
    categories {
      id name description parentId parentName createdAt updatedAt
    }
  }
`;

const CATEGORY_ROOTS_QUERY = gql`
  query GetCategoryRoots {
    categoryRoots {
      id name description parentId parentName createdAt updatedAt
    }
  }
`;

const CATEGORY_CHILDREN_QUERY = gql`
  query GetCategoryChildren($parentId: Long!) {
    categoryChildren(parentId: $parentId) {
      id name description parentId parentName createdAt updatedAt
    }
  }
`;

const CATEGORY_QUERY = gql`
  query GetCategory($id: Long!) {
    category(id: $id) {
      id name description parentId parentName createdAt updatedAt
    }
  }
`;

const CATEGORY_TREE_QUERY = gql`
  query GetCategoryTree($id: Long!) {
    categoryTree(id: $id) {
      id name description createdAt updatedAt
      children {
        id name description createdAt updatedAt
        children {
          id name description createdAt updatedAt
          children {
            id name description createdAt updatedAt
          }
        }
      }
    }
  }
`;

const CREATE_CATEGORY_MUTATION = gql`
  mutation CreateCategory($input: CreateCategoryInput!) {
    createCategory(input: $input) {
      id name description parentId parentName createdAt updatedAt
    }
  }
`;

const UPDATE_CATEGORY_MUTATION = gql`
  mutation UpdateCategory($id: Long!, $input: UpdateCategoryInput!) {
    updateCategory(id: $id, input: $input) {
      id name description parentId parentName createdAt updatedAt
    }
  }
`;

const DELETE_CATEGORY_MUTATION = gql`
  mutation DeleteCategory($id: Long!) {
    deleteCategory(id: $id)
  }
`;

@Injectable({ providedIn: 'root' })
export class CategoriesGraphqlService {
  private readonly apollo = inject(Apollo);

  findAll(): Observable<CategoryResponse[]> {
    return this.apollo.query<{ categories: CategoryResponse[] }>({
      query: CATEGORIES_QUERY,
    }).pipe(map(result => result.data!.categories));
  }

  findRoots(): Observable<CategoryResponse[]> {
    return this.apollo.query<{ categoryRoots: CategoryResponse[] }>({
      query: CATEGORY_ROOTS_QUERY,
    }).pipe(map(result => result.data!.categoryRoots));
  }

  findChildren(parentId: number): Observable<CategoryResponse[]> {
    return this.apollo.query<{ categoryChildren: CategoryResponse[] }>({
      query: CATEGORY_CHILDREN_QUERY,
      variables: { parentId },
    }).pipe(map(result => result.data!.categoryChildren));
  }

  findById(categoryId: number): Observable<CategoryResponse> {
    return this.apollo.query<{ category: CategoryResponse }>({
      query: CATEGORY_QUERY,
      variables: { id: categoryId },
    }).pipe(map(result => result.data!.category));
  }

  findTree(rootCategoryId: number): Observable<CategoryTreeResponse> {
    return this.apollo.query<{ categoryTree: CategoryTreeResponse }>({
      query: CATEGORY_TREE_QUERY,
      variables: { id: rootCategoryId },
    }).pipe(map(result => result.data!.categoryTree));
  }

  create(payload: CreateCategoryRequest): Observable<CategoryResponse> {
    return this.apollo.mutate<{ createCategory: CategoryResponse }>({
      mutation: CREATE_CATEGORY_MUTATION,
      variables: { input: payload },
    }).pipe(map(result => result.data!.createCategory));
  }

  update(categoryId: number, payload: UpdateCategoryRequest): Observable<CategoryResponse> {
    return this.apollo.mutate<{ updateCategory: CategoryResponse }>({
      mutation: UPDATE_CATEGORY_MUTATION,
      variables: { id: categoryId, input: payload },
    }).pipe(map(result => result.data!.updateCategory));
  }

  delete(categoryId: number): Observable<boolean> {
    return this.apollo.mutate<{ deleteCategory: boolean }>({
      mutation: DELETE_CATEGORY_MUTATION,
      variables: { id: categoryId },
    }).pipe(map(result => result.data!.deleteCategory));
  }
}
