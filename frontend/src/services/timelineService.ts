import api from './api';
import type { TimelineEvent } from '../types/timeline';

export async function getTimeline(leadId: string): Promise<TimelineEvent[]> {
  const r = await api.get<{ data: TimelineEvent[] }>(`/leads/${leadId}/timeline`);
  return r.data.data;
}
