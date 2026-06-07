import { useState, useEffect } from 'react';
import * as taskService from '../services/taskService';
import type { Task } from '../types/task';

const PRIORITY_COLORS: Record<string, string> = {
  ALTA: 'text-red-600 bg-red-50',
  MEDIA: 'text-yellow-600 bg-yellow-50',
  BAIXA: 'text-green-600 bg-green-50',
};

const STATUS_COLORS: Record<string, string> = {
  NO_PRAZO: 'text-green-600',
  COM_ATRASO: 'text-yellow-600',
  VENCIDA: 'text-red-600 font-bold',
};

export default function TasksPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    taskService.myTasks().then(setTasks).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const handleComplete = async (id: string) => {
    await taskService.completeTask(id);
    setTasks((prev) => prev.filter((t) => t.id !== id));
  };

  if (loading) return <div className="p-8 text-center text-gray-500">Carregando...</div>;

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Minhas Tarefas</h1>
      {tasks.length === 0 ? (
        <p className="text-gray-400 text-center py-8">Nenhuma tarefa pendente</p>
      ) : (
        <div className="space-y-3">
          {tasks.map((task) => (
            <div key={task.id} className="bg-white rounded-lg shadow-sm border p-4">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <p className="font-medium">{task.title}</p>
                  {task.description && <p className="text-sm text-gray-500 mt-1">{task.description}</p>}
                  <div className="flex gap-3 mt-2 text-xs">
                    <span className={PRIORITY_COLORS[task.priority] || '' + ' px-2 py-0.5 rounded'}>
                      {task.priority}
                    </span>
                    {task.completionStatus && (
                      <span className={STATUS_COLORS[task.completionStatus] || ''}>
                        {task.completionStatus}
                      </span>
                    )}
                    {task.dueDate && <span className="text-gray-400">Prazo: {new Date(task.dueDate).toLocaleDateString('pt-BR')}</span>}
                  </div>
                </div>
                {task.status === 'PENDENTE' && (
                  <button onClick={() => handleComplete(task.id)}
                    className="text-sm text-blue-600 hover:underline ml-4 whitespace-nowrap">
                    Concluir
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
