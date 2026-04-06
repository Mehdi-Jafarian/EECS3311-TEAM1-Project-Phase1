import { useState } from 'react';
import api from '../api';

export default function ChatWidget() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  const send = async () => {
    if (!input.trim()) return;
    const userMsg = input.trim();
    setInput('');
    const newMessages = [...messages, { role: 'user', content: userMsg }];
    setMessages(newMessages);
    setLoading(true);

    try {
      const history = newMessages.slice(0, -1).map(m => ({ role: m.role, content: m.content }));
      const res = await api.post('/chat', { message: userMsg, conversationHistory: history });
      setMessages([...newMessages, { role: 'assistant', content: res.data.response }]);
    } catch {
      setMessages([...newMessages, { role: 'assistant', content: 'Sorry, something went wrong.' }]);
    }
    setLoading(false);
  };

  const handleKey = (e) => { if (e.key === 'Enter') send(); };

  if (!open) {
    return (
      <button onClick={() => setOpen(true)} style={{
        position: 'fixed', bottom: 24, right: 24, width: 56, height: 56,
        borderRadius: '50%', background: '#1976d2', color: '#fff', border: 'none',
        fontSize: 24, cursor: 'pointer', boxShadow: '0 4px 12px rgba(0,0,0,0.3)', zIndex: 1000
      }}>
        💬
      </button>
    );
  }

  return (
    <div style={{
      position: 'fixed', bottom: 24, right: 24, width: 380, height: 500,
      background: '#fff', borderRadius: 12, boxShadow: '0 8px 32px rgba(0,0,0,0.2)',
      display: 'flex', flexDirection: 'column', zIndex: 1000, overflow: 'hidden'
    }}>
      <div style={{ background: '#1976d2', color: '#fff', padding: '12px 16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <strong>AI Assistant</strong>
        <button onClick={() => setOpen(false)} style={{ background: 'none', border: 'none', color: '#fff', fontSize: 18, cursor: 'pointer' }}>✕</button>
      </div>

      <div style={{ flex: 1, overflowY: 'auto', padding: 12 }}>
        {messages.length === 0 && (
          <p style={{ color: '#999', textAlign: 'center', marginTop: 40 }}>Ask me anything about the platform!</p>
        )}
        {messages.map((m, i) => (
          <div key={i} style={{
            marginBottom: 8, display: 'flex',
            justifyContent: m.role === 'user' ? 'flex-end' : 'flex-start'
          }}>
            <div style={{
              maxWidth: '80%', padding: '8px 12px', borderRadius: 12,
              background: m.role === 'user' ? '#1976d2' : '#f0f0f0',
              color: m.role === 'user' ? '#fff' : '#333',
              whiteSpace: 'pre-wrap', fontSize: 14
            }}>
              {m.content}
            </div>
          </div>
        ))}
        {loading && (
          <div style={{ display: 'flex', justifyContent: 'flex-start', marginBottom: 8 }}>
            <div style={{ padding: '8px 12px', borderRadius: 12, background: '#f0f0f0', color: '#999' }}>
              Thinking...
            </div>
          </div>
        )}
      </div>

      <div style={{ padding: 12, borderTop: '1px solid #eee', display: 'flex', gap: 8 }}>
        <input value={input} onChange={e => setInput(e.target.value)} onKeyDown={handleKey}
          placeholder="Type your question..." style={{ flex: 1, margin: 0 }} />
        <button className="btn btn-primary" onClick={send} disabled={loading || !input.trim()}>
          Send
        </button>
      </div>
    </div>
  );
}
