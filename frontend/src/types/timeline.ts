export interface TimelineEvent {
  id: string;
  type: string;
  userName: string;
  metadata: Record<string, unknown> | null;
  createdAt: string;
}
