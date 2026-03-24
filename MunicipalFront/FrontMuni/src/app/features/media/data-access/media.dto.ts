export interface MediaAssetResponse {
  id: number;
  publicId: string;
  secureUrl: string;
  resourceType: string;
  format: string;
  originalFilename: string;
  contentType: string;
  sizeInBytes: number;
  width: number | null;
  height: number | null;
  createdAt: string;
}

export type MediaFolder = 'BOOKS' | 'USERS' | 'AUTHORS' | 'CATEGORIES';
