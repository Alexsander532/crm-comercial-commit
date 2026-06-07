import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as leadService from '../services/leadService';
import type { LeadRequest } from '../types/lead';

const segmentOptions = [
  'TECNOLOGIA', 'FINANCAS', 'SAUDE', 'EDUCACAO', 'VAREJO', 'OUTRO',
];

export default function LeadFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEditing = !!id;

  const [form, setForm] = useState<LeadRequest>({
    companyName: '',
    segment: 'OUTRO' as LeadRequest['segment'],
    address: '',
    site: '',
    instagram: '',
    whatsapp: '',
    notes: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (id) {
      leadService.getLead(id).then((lead) => {
        setForm({
          companyName: lead.companyName,
          segment: lead.segment,
          address: lead.address || '',
          site: lead.site || '',
          instagram: lead.instagram || '',
          whatsapp: lead.whatsapp || '',
          notes: lead.notes || '',
        });
      });
    }
  }, [id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      if (isEditing) {
        await leadService.updateLead(id!, form);
      } else {
        await leadService.createLead(form);
      }
      navigate('/leads');
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || 'Erro ao salvar lead');
    } finally {
      setSubmitting(false);
    }
  };

  const updateField = (field: keyof LeadRequest, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <button onClick={() => navigate('/leads')} className="text-blue-600 hover:underline mb-4 block">
        ← Voltar
      </button>

      <div className="bg-white rounded-lg shadow p-6">
        <h1 className="text-2xl font-bold mb-6">{isEditing ? 'Editar Lead' : 'Novo Lead'}</h1>

        {error && (
          <div className="bg-red-50 text-red-700 px-4 py-3 rounded-lg text-sm mb-4">{error}</div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nome da empresa *
            </label>
            <input
              required
              value={form.companyName}
              onChange={(e) => updateField('companyName', e.target.value)}
              className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Segmento *</label>
            <select
              value={form.segment as string}
              onChange={(e) => updateField('segment', e.target.value)}
              className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500"
            >
              {segmentOptions.map((opt) => (
                <option key={opt} value={opt}>{opt}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Endereço</label>
            <input
              value={form.address || ''}
              onChange={(e) => updateField('address', e.target.value)}
              className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Site</label>
              <input
                value={form.site || ''}
                onChange={(e) => updateField('site', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Instagram</label>
              <input
                value={form.instagram || ''}
                onChange={(e) => updateField('instagram', e.target.value)}
                className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">WhatsApp</label>
            <input
              value={form.whatsapp || ''}
              onChange={(e) => updateField('whatsapp', e.target.value)}
              className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Observações</label>
            <textarea
              value={form.notes || ''}
              onChange={(e) => updateField('notes', e.target.value)}
              rows={3}
              className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="submit"
              disabled={submitting}
              className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
              {submitting ? 'Salvando...' : 'Salvar'}
            </button>
            <button
              type="button"
              onClick={() => navigate('/leads')}
              className="bg-gray-100 text-gray-700 px-6 py-2 rounded-lg hover:bg-gray-200"
            >
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
