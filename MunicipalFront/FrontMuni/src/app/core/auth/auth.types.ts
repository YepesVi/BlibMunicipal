import { UserRole } from '../../features/auth/data-access/auth.dto';

export interface AuthSession {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  username: string;
  role: UserRole;
}
