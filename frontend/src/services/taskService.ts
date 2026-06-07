import api from './api';
import type { Task, TaskRequest } from '../types/task';

export async function listLeadTasks(leadId: string): Promise<Task[]> {
  const r = await api.get<{ data: Task[] }>(`/leads/${leadId}/tasks`);
  return r.data.data;
}

export async function createTask(leadId: string, data: TaskRequest): Promise<Task> {
  const r = await api.post<{ data: Task }>(`/leads/${leadId}/tasks`, data);
  return r.data.data;
}

export async function completeTask(id: string): Promise<Task> {
  const r = await api.patch<{ data: Task }>(`/tasks/${id}/complete`);
  return r.data.data;
}

export async function cancelTask(id: string): Promise<void> {
  await api.patch(`/tasks/${id}/cancel`);
}

export async function myTasks(): Promise<Task[]> {
  const r = await api.get<{ data: Task[] }>('/tasks/my');
  return r.data.data;
}
