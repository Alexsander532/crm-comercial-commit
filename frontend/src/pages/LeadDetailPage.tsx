import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as leadService from '../services/leadService';
import type { Lead } from '../types/lead';

export default function LeadDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [lead, setLead] = useState<Lead | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      leadService
        .getLead(id)
        .then(setLead)
        .catch(() => navigate('/leads'))
        .finally(() => setLoading(false));
    }
  }, [id, navigate]);

  if (loading) return <div className="text-center py-8 text-gray-500">Carregando...</div>;
  if (!lead) return <div className="text-center py-8 text-gray-500">Lead não encontrado</div>;

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <button onClick={() => navigate('/leads')} className="text-blue-600 hover:underline mb-4 block">
        ← Voltar
      </button>

      <div className="bg-white rounded-lg shadow p-6">
        <h1 className="text-2xl font-bold mb-4">{lead.companyName}</h1>

        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <span className="text-gray-500">Segmento:</span>
            <p className="font-medium">{lead.segment}</p>
          </div>
          <div>
            <span className="text-gray-500">Status:</span>
            <p className="font-medium">{lead.status}</p>
          </div>
          <div>
            <span className="text-gray-500">Endereço:</span>
            <p className="font-medium">{lead.address || '—'}</p>
          </div>
          <div>
            <span className="text-gray-500">Site:</span>
            <p className="font-medium">{lead.site || '—'}</p>
          </div>
          <div>
            <span className="text-gray-500">Instagram:</span>
            <p className="font-medium">{lead.instagram || '—'}</p>
          </div>
          <div>
            <span className="text-gray-500">WhatsApp:</span>
            <p className="font-medium">{lead.whatsapp || '—'}</p>
          </div>
          <div>
            <span className="text-gray-500">Criado por:</span>
            <p className="font-medium">{lead.createdByName}</p>
          </div>
          <div>
            <span className="text-gray-500">Atribuído a:</span>
            <p className="font-medium">{lead.assignedToName || '—'}</p>
          </div>
        </div>

        {lead.notes && (
          <div className="mt-4">
            <span className="text-gray-500 text-sm">Observações:</span>
            <p className="mt-1 text-gray-700">{lead.notes}</p>
          </div>
        )}

        <div className="flex gap-3 mt-6">
          <button
            onClick={() => navigate(`/leads/${id}/edit`)}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 text-sm"
          >
            Editar
          </button>
        </div>
      </div>
    </div>
  );
}
