import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as leadService from '../services/leadService';
import * as contactService from '../services/contactService';
import type { Lead } from '../types/lead';
import type { Contact, ContactRequest } from '../types/contact';

export default function LeadDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [lead, setLead] = useState<Lead | null>(null);
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<ContactRequest>({ name: '' });

  const loadData = async () => {
    if (!id) return;
    try {
      const [leadData, contactsData] = await Promise.all([
        leadService.getLead(id),
        contactService.listContacts(id),
      ]);
      setLead(leadData);
      setContacts(contactsData);
    } catch {
      navigate('/leads');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, [id]);

  const handleAddContact = async () => {
    if (!id || !form.name) return;
    await contactService.createContact(id, form);
    setForm({ name: '' });
    setShowForm(false);
    loadData();
  };

  const handleDeleteContact = async (contactId: string) => {
    if (!id || !confirm('Remover contato?')) return;
    await contactService.deleteContact(id, contactId);
    loadData();
  };

  const handleSetMain = async (contactId: string) => {
    if (!id) return;
    await contactService.setMainContact(id, contactId);
    loadData();
  };

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

        {/* Contatos */}
        <div className="mt-6">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-lg font-semibold">Contatos ({contacts.length})</h2>
            <button
              onClick={() => setShowForm(!showForm)}
              className="text-blue-600 hover:underline text-sm"
            >
              {showForm ? 'Cancelar' : '+ Adicionar'}
            </button>
          </div>

          {showForm && (
            <div className="bg-gray-50 p-4 rounded-lg mb-4 space-y-3">
              <input
                placeholder="Nome *"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                className="w-full px-3 py-2 border rounded-lg text-sm"
              />
              <div className="grid grid-cols-2 gap-3">
                <input placeholder="Cargo" value={form.role || ''}
                  onChange={(e) => setForm({ ...form, role: e.target.value })}
                  className="px-3 py-2 border rounded-lg text-sm" />
                <input placeholder="Telefone" value={form.phone || ''}
                  onChange={(e) => setForm({ ...form, phone: e.target.value })}
                  className="px-3 py-2 border rounded-lg text-sm" />
                <input placeholder="Email" value={form.email || ''}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  className="px-3 py-2 border rounded-lg text-sm" />
                <input placeholder="WhatsApp" value={form.whatsapp || ''}
                  onChange={(e) => setForm({ ...form, whatsapp: e.target.value })}
                  className="px-3 py-2 border rounded-lg text-sm" />
              </div>
              <button
                onClick={handleAddContact}
                disabled={!form.name}
                className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-700 disabled:opacity-50"
              >
                Salvar
              </button>
            </div>
          )}

          {contacts.length === 0 ? (
            <p className="text-gray-400 text-sm">Nenhum contato cadastrado</p>
          ) : (
            <div className="space-y-2">
              {contacts.map((c) => (
                <div key={c.id} className="flex items-center justify-between bg-gray-50 p-3 rounded-lg">
                  <div>
                    <p className="font-medium text-sm">
                      {c.name}
                      {c.isMain && <span className="ml-2 text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded">Principal</span>}
                    </p>
                    <p className="text-xs text-gray-500">
                      {[c.role, c.phone, c.email, c.whatsapp].filter(Boolean).join(' | ')}
                    </p>
                  </div>
                  <div className="flex gap-2">
                    {!c.isMain && (
                      <button onClick={() => handleSetMain(c.id)}
                        className="text-xs text-blue-600 hover:underline">Principal</button>
                    )}
                    <button onClick={() => handleDeleteContact(c.id)}
                      className="text-xs text-red-600 hover:underline">Remover</button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

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
