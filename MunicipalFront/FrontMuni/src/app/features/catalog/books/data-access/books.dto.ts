export interface BookSummaryResponse {
  id: number;
  isbn: string;
  title: string;
  publisher: string;
  publicationYear: number;
  authorId: number;
  authorName: string;
  categoryId: number;
  categoryName: string;
  primaryImageUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface BookResponse {
  id: number;
  isbn: string;
  title: string;
  publisher: string;
  publicationYear: number;
  description: string | null;
  authorId: number;
  authorName: string;
  categoryId: number;
  categoryName: string;
  primaryImageUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBookRequest {
  isbn: string;
  title: string;
  publisher: string;
  publicationYear: number;
  description?: string;
  authorId: number;
  categoryId: number;
}

export interface UpdateBookRequest extends CreateBookRequest {}

export interface AttachBookImageItemRequest {
  mediaAssetId: number;
  primaryImage?: boolean;
  altText?: string;
}

export interface AttachBookImagesRequest {
  images: AttachBookImageItemRequest[];
}

export interface BooksQueryParams {
  title?: string;
  authorId?: number;
  categoryId?: number;
  publicationYear?: number;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}
