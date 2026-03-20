export interface AuthorResponse {
  id: number;
  idCard: string;
  fullName: string;
  nationality: string;
  biography: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAuthorRequest {
  idCard: string;
  fullName: string;
  nationality: string;
  biography?: string;
}

export interface UpdateAuthorRequest {
  idCard: string;
  fullName: string;
  nationality: string;
  biography?: string;
}
