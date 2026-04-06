import { Injectable, inject } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { map, Observable } from 'rxjs';

import { MediaAssetResponse } from './media.dto';

const MEDIA_ASSETS_QUERY = gql`
  query GetMediaAssets {
    mediaAssets {
      id publicId secureUrl resourceType format
      originalFilename contentType sizeInBytes width height createdAt
    }
  }
`;

const MEDIA_ASSET_QUERY = gql`
  query GetMediaAsset($id: Long!) {
    mediaAsset(id: $id) {
      id publicId secureUrl resourceType format
      originalFilename contentType sizeInBytes width height createdAt
    }
  }
`;

const DELETE_MEDIA_ASSET_MUTATION = gql`
  mutation DeleteMediaAsset($id: Long!) {
    deleteMediaAsset(id: $id)
  }
`;

@Injectable({ providedIn: 'root' })
export class MediaGraphqlService {
  private readonly apollo = inject(Apollo);

  findAll(): Observable<MediaAssetResponse[]> {
    return this.apollo.query<{ mediaAssets: MediaAssetResponse[] }>({
      query: MEDIA_ASSETS_QUERY,
    }).pipe(map(result => result.data!.mediaAssets));
  }

  findById(mediaAssetId: number): Observable<MediaAssetResponse> {
    return this.apollo.query<{ mediaAsset: MediaAssetResponse }>({
      query: MEDIA_ASSET_QUERY,
      variables: { id: mediaAssetId },
    }).pipe(map(result => result.data!.mediaAsset));
  }

  delete(mediaAssetId: number): Observable<boolean> {
    return this.apollo.mutate<{ deleteMediaAsset: boolean }>({
      mutation: DELETE_MEDIA_ASSET_MUTATION,
      variables: { id: mediaAssetId },
    }).pipe(map(result => result.data!.deleteMediaAsset));
  }
}
