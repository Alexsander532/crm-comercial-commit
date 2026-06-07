export interface Task {
  id: string;
  leadId: string;
  title: string;
  description: string | null;
  priority: string;
  status: string;
  completionStatus: string | null;
  daysOverdue: number;
  assignedToName: string | null;
  createdByName: string;
  dueDate: string | null;
  completedAt: string | null;
  createdAt: string;
}

export interface TaskRequest {
  title: string;
  description?: string;
  priority: string;
  assignedToId: string;
  dueDate?: string;
}
