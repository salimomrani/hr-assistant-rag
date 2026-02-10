export interface DashboardAnalytics {
  totalQuestionsToday: number;
  averageResponseTimeMs: number;
  totalDocuments: number;
  conversationsThisWeek: number;
  popularQuestions: QuestionFrequency[];
  topDocuments: DocumentReference[];
  dailyUsage: DailyCount[];
  dailyResponseTimes: DailyAverage[];
}

export interface QuestionFrequency {
  question: string;
  count: number;
}

export interface DocumentReference {
  documentId: string;
  documentName: string;
  referenceCount: number;
}

export interface DailyCount {
  date: string;
  count: number;
}

export interface DailyAverage {
  date: string;
  averageMs: number;
}
