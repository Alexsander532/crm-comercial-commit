import { useState, useEffect } from 'react';
import api from '../services/api';

interface DashboardData {
  summary: { totalLeads: number; leadsThisMonth: number; tasksPending: number; tasksOverdue: number };
  pipelineChart: Record<string, number>;
  recentTasks: Array<{ title: string; status: string; leadName: string }>;
}

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [, setLoading] = useState(true);

  useEffect(() => {
    api.get<{ data: DashboardData }>('/dashboard')
      .then((r) => setData(r.data.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (!data) return <div className="p-8 text-center text-gray-500">Carregando dashboard...</div>;

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Dashboard</h1>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <Card label="Total de Leads" value={data.summary.totalLeads} color="blue" />
        <Card label="Leads (30 dias)" value={data.summary.leadsThisMonth} color="green" />
        <Card label="Tarefas Pendentes" value={data.summary.tasksPending} color="yellow" />
        <Card label="Tarefas Atrasadas" value={data.summary.tasksOverdue} color="red" />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-4">
          <h2 className="font-semibold mb-3">Pipeline</h2>
          <div className="space-y-2">
            {Object.entries(data.pipelineChart).map(([status, count]) => (
              <div key={status} className="flex justify-between text-sm">
                <span>{status}</span>
                <span className="font-medium">{count}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-4">
          <h2 className="font-semibold mb-3">Tarefas Recentes</h2>
          {data.recentTasks.length === 0 ? (
            <p className="text-sm text-gray-400">Nenhuma tarefa pendente</p>
          ) : (
            <div className="space-y-2">
              {data.recentTasks.map((t, i) => (
                <div key={i} className="text-sm flex justify-between">
                  <span>{t.title}</span>
                  <span className="text-gray-500">{t.leadName}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function Card({ label, value, color }: { label: string; value: number; color: string }) {
  const colors: Record<string, string> = {
    blue: 'bg-blue-50 border-blue-200 text-blue-700',
    green: 'bg-green-50 border-green-200 text-green-700',
    yellow: 'bg-yellow-50 border-yellow-200 text-yellow-700',
    red: 'bg-red-50 border-red-200 text-red-700',
  };
  return (
    <div className={`rounded-lg border p-4 ${colors[color] || colors.blue}`}>
      <p className="text-xs opacity-75">{label}</p>
      <p className="text-2xl font-bold mt-1">{value}</p>
    </div>
  );
}
