import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import * as leadService from '../services/leadService';
import type { Lead } from '../types/lead';

export default function LeadsPage() {
  const [leads, setLeads] = useState<Lead[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const navigate = useNavigate();

  const loadLeads = useCallback(async () => {
    try {
      setLoading(true);
      const data = search
        ? await leadService.searchLeads(search)
        : await leadService.listLeads();
      setLeads(data.content);
    } catch (err) {
      console.error('Erro ao carregar leads', err);
    } finally {
      setLoading(false);
    }
  }, [search]);

  useEffect(() => {
    loadLeads();
  }, [loadLeads]);

  const handleArchive = async (id: string) => {
    if (!confirm('Arquivar este lead?')) return;
    try {
      await leadService.archiveLead(id);
      loadLeads();
    } catch (err) {
      console.error('Erro ao arquivar', err);
    }
  };

  return (
    <div className="p-6 max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Leads</h1>
        <button
          onClick={() => navigate('/leads/new')}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
        >
          Novo Lead
        </button>
      </div>

      <input
        type="text"
        placeholder="Buscar leads..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        className="w-full px-4 py-2 border border-gray-300 rounded-lg mb-4 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
      />

      {loading ? (
        <div className="text-center text-gray-500 py-8">Carregando...</div>
      ) : leads.length === 0 ? (
        <div className="text-center text-gray-500 py-8">Nenhum lead encontrado</div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="text-left px-4 py-3 text-sm font-medium text-gray-600">Empresa</th>
                <th className="text-left px-4 py-3 text-sm font-medium text-gray-600">Segmento</th>
                <th className="text-left px-4 py-3 text-sm font-medium text-gray-600">Status</th>
                <th className="text-left px-4 py-3 text-sm font-medium text-gray-600">Atribuído a</th>
                <th className="text-right px-4 py-3 text-sm font-medium text-gray-600">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {leads.map((lead) => (
                <tr key={lead.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <button
                      onClick={() => navigate(`/leads/${lead.id}`)}
                      className="text-blue-600 hover:underline font-medium"
                    >
                      {lead.companyName}
                    </button>
                  </td>
                  <td className="px-4 py-3 text-sm">{lead.segment}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex px-2 py-1 rounded-full text-xs font-medium
                      ${lead.status === 'NOVO' ? 'bg-blue-100 text-blue-800' : ''}
                      ${lead.status === 'CONTATO' ? 'bg-yellow-100 text-yellow-800' : ''}
                      ${lead.status === 'NEGOCIACAO' ? 'bg-purple-100 text-purple-800' : ''}
                      ${lead.status === 'GANHO' ? 'bg-green-100 text-green-800' : ''}
                      ${lead.status === 'PERDIDO' ? 'bg-red-100 text-red-800' : ''}
                      ${lead.status === 'ARQUIVADO' ? 'bg-gray-100 text-gray-800' : ''}
                    `}>
                      {lead.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {lead.assignedToName || '—'}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button
                      onClick={() => navigate(`/leads/${lead.id}`)}
                      className="text-blue-600 hover:underline text-sm mr-3"
                    >
                      Editar
                    </button>
                    <button
                      onClick={() => handleArchive(lead.id)}
                      className="text-red-600 hover:underline text-sm"
                    >
                      Arquivar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
