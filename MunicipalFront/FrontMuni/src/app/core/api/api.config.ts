import { environment } from '../../../environments/environment';

const trimSlash = (value: string): string => value.replace(/\/+$/, '');

const trimLeadingSlash = (value: string): string => value.replace(/^\/+/, '');

export const getApiUrl = (path: string): string => {
  const baseUrl = trimSlash(environment.apiBaseUrl);
  const normalizedPath = trimLeadingSlash(path);
  return `${baseUrl}/${normalizedPath}`;
};
