import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import * as leadService from '../services/leadService';
import type { Lead } from '../types/lead';

const COLUMNS = ['NOVO', 'CONTATO', 'NEGOCIACAO', 'GANHO', 'PERDIDO', 'ARQUIVADO'];

const STATUS_COLORS: Record<string, string> = {
  NOVO: 'border-blue-400 bg-blue-50',
  CONTATO: 'border-yellow-400 bg-yellow-50',
  NEGOCIACAO: 'border-purple-400 bg-purple-50',
  GANHO: 'border-green-500 bg-green-50',
  PERDIDO: 'border-red-400 bg-red-50',
  ARQUIVADO: 'border-gray-400 bg-gray-50',
};

export default function KanbanPage() {
  const [leads, setLeads] = useState<Lead[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    leadService.listLeads(0, 100).then((data) => {
      setLeads(data.content);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const getColumnLeads = (status: string) =>
    leads.filter((l) => l.status === status);

  if (loading) {
    return <div className="p-8 text-center text-gray-500">Carregando kanban...</div>;
  }

  return (
    <div className="p-6 overflow-x-auto">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Pipeline</h1>
      <div className="flex gap-4 min-w-max">
        {COLUMNS.map((status) => {
          const columnLeads = getColumnLeads(status);
          return (
            <div key={status} className={`w-72 rounded-lg border-2 ${STATUS_COLORS[status] || 'border-gray-200'}`}>
              <div className="px-4 py-3 border-b border-gray-200 bg-white bg-opacity-50 rounded-t-lg">
                <h2 className="font-semibold text-gray-700 text-sm flex items-center justify-between">
                  {status}
                  <span className="bg-gray-200 text-gray-600 text-xs px-2 py-0.5 rounded-full">
                    {columnLeads.length}
                  </span>
                </h2>
              </div>
              <div className="p-3 space-y-3 min-h-[200px]">
                {columnLeads.map((lead) => (
                  <div key={lead.id}
                    onClick={() => navigate(`/leads/${lead.id}`)}
                    className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 cursor-pointer hover:shadow-md transition-shadow"
                  >
                    <p className="font-medium text-gray-800 text-sm mb-1">{lead.companyName}</p>
                    <p className="text-xs text-gray-500 mb-2">{lead.segment}</p>
                    {lead.assignedToName && (
                      <p className="text-xs text-blue-600">👤 {lead.assignedToName}</p>
                    )}
                  </div>
                ))}
                {columnLeads.length === 0 && (
                  <p className="text-xs text-gray-400 text-center py-4">Nenhum lead</p>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
