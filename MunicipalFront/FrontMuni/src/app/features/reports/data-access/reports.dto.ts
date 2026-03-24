export interface BooksByAuthorReportItemResponse {
  bookId: number;
  isbn: string;
  title: string;
  publisher: string;
  publicationYear: number;
  categoryName: string;
  primaryImageUrl: string | null;
}

export interface BooksByAuthorReportResponse {
  authorIdCard: string;
  authorName: string;
  generatedAt: string;
  totalBooks: number;
  books: BooksByAuthorReportItemResponse[];
}
