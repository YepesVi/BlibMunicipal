import { UserRole } from '../../auth/data-access/auth.dto';

export interface UserResponse {
  id: number;
  username: string;
  role: UserRole;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  role: UserRole;
}

export interface UpdateUserRequest {
  username: string;
  password?: string;
  role: UserRole;
}

export interface UsersQueryParams {
  username?: string;
  role?: UserRole;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}
