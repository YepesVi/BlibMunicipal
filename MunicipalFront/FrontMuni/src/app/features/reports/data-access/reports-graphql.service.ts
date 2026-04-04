import { Injectable, inject } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { map, Observable } from 'rxjs';

import { BooksByAuthorReportResponse } from './reports.dto';

const BOOKS_BY_AUTHOR_REPORT_QUERY = gql`
  query GetBooksByAuthorReport($authorIdCard: String!) {
    booksByAuthorReport(authorIdCard: $authorIdCard) {
      authorIdCard authorName generatedAt totalBooks
      books {
        bookId isbn title publisher publicationYear
        categoryName primaryImageUrl
      }
    }
  }
`;

@Injectable({ providedIn: 'root' })
export class ReportsGraphqlService {
  private readonly apollo = inject(Apollo);

  getBooksByAuthorIdCard(idCard: string): Observable<BooksByAuthorReportResponse> {
    return this.apollo.query<{ booksByAuthorReport: BooksByAuthorReportResponse }>({
      query: BOOKS_BY_AUTHOR_REPORT_QUERY,
      variables: { authorIdCard: idCard },
    }).pipe(map(result => result.data!.booksByAuthorReport));
  }
}
