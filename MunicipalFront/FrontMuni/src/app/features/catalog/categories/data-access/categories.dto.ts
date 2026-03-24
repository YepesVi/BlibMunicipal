export interface CategoryResponse {
  id: number;
  name: string;
  description: string | null;
  parentId: number | null;
  parentName: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CategoryTreeResponse {
  id: number;
  name: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
  children: CategoryTreeResponse[];
}

export interface CreateCategoryRequest {
  name: string;
  description?: string;
  parentId?: number | null;
}

export interface UpdateCategoryRequest {
  name: string;
  description?: string;
  parentId?: number | null;
}
