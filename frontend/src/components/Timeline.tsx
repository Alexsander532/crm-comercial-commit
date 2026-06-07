import { useState, useEffect } from 'react';
import * as timelineService from '../services/timelineService';
import type { TimelineEvent } from '../types/timeline';

const ICONS: Record<string, string> = {
  CREATED: '🆕', STATUS_CHANGED: '🔄', FIELD_UPDATED: '✏️',
  INTERACTION: '📞', NOTE_ADDED: '📝', TASK_CREATED: '📋',
  TASK_COMPLETED: '✅', ASSIGNED: '👤', CONTACT_ADDED: '👥',
  CONTACT_UPDATED: '👤',
};

export default function Timeline({ leadId }: { leadId: string }) {
  const [events, setEvents] = useState<TimelineEvent[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    timelineService.getTimeline(leadId).then(setEvents).catch(() => {}).finally(() => setLoading(false));
  }, [leadId]);

  if (loading) return <div className="text-sm text-gray-400">Carregando timeline...</div>;

  return (
    <div className="space-y-3">
      {events.length === 0 && <p className="text-sm text-gray-400">Nenhum evento registrado</p>}
      {events.map((e) => (
        <div key={e.id} className="flex gap-3 text-sm">
          <span className="text-lg w-6 text-center">{ICONS[e.type] || '📌'}</span>
          <div>
            <p className="text-gray-700">
              <span className="font-medium">{e.userName}</span>
              {' '}{formatAction(e)}
            </p>
            <p className="text-gray-400 text-xs">{formatDate(e.createdAt)}</p>
          </div>
        </div>
      ))}
    </div>
  );
}

function formatAction(e: TimelineEvent): string {
  switch (e.type) {
    case 'CREATED': return 'criou este lead';
    case 'STATUS_CHANGED': return `moveu de ${e.metadata?.from} para ${e.metadata?.to}`;
    case 'FIELD_UPDATED': return `alterou ${e.metadata?.field}: ${e.metadata?.oldValue} → ${e.metadata?.newValue}`;
    case 'INTERACTION': return `registrou ${e.metadata?.type}: ${e.metadata?.description}`;
    case 'TASK_CREATED': return `criou tarefa "${e.metadata?.title}"`;
    case 'TASK_COMPLETED': return `concluiu tarefa "${e.metadata?.title}"`;
    case 'ASSIGNED': return `atribuiu lead`;
    case 'CONTACT_ADDED': return `adicionou contato "${e.metadata?.name}"`;
    default: return e.type;
  }
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleString('pt-BR');
}
