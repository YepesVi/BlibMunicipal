export interface Libro {
    id?: number;
    isbn: string;
    title: string;
    publisher: string;
    publicationYear: number;
    description?: string;
    authorId: number;
    authorName?: string;
    categoryId: number;
    categoryName?: string;
    primaryImageUrl?: string;
    images?: any[];
}

export interface LibroSummary {
    id: number;
    isbn: string;
    title: string;
    publisher: string;
    publicationYear: number;
    authorId: number;
    authorName: string;
    categoryId: number;
    categoryName: string;
    primaryImageUrl?: string;
    createdAt?: string;
    updatedAt?: string;
}
