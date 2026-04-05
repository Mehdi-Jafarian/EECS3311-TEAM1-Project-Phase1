import { useState, useEffect, useCallback } from 'react';
import api from '../../api';

export default function ConsultantApproval() {
  const [pending, setPending] = useState([]);
  const [message, setMessage] = useState(null);

  const load = useCallback(() => {
    api.get('/admin/consultants/pending').then(res => setPending(res.data));
  }, []);

  useEffect(() => { load(); }, [load]);

  const approve = async (id) => {
    try {
      await api.put(`/admin/consultants/${id}/approve`);
      setMessage({ type: 'success', text: 'Consultant approved' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Failed' });
    }
  };

  const reject = async (id) => {
    try {
      await api.put(`/admin/consultants/${id}/reject`);
      setMessage({ type: 'success', text: 'Consultant rejected' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Failed' });
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>Pending Consultant Approvals</h2>
      {message && (
        <div className="card" style={{ background: message.type === 'success' ? '#e8f5e9' : '#ffebee' }}>
          {message.text}
        </div>
      )}
      <table>
        <thead>
          <tr><th>Name</th><th>Email</th><th>Status</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {pending.map(c => (
            <tr key={c.id}>
              <td>{c.name}</td>
              <td>{c.email}</td>
              <td><span className="badge badge-requested">{c.status}</span></td>
              <td>
                <button className="btn btn-success" onClick={() => approve(c.id)}>Approve</button>
                <button className="btn btn-danger" onClick={() => reject(c.id)}>Reject</button>
              </td>
            </tr>
          ))}
          {pending.length === 0 && <tr><td colSpan={4} style={{ textAlign: 'center', color: '#999' }}>No pending consultants</td></tr>}
        </tbody>
      </table>
      <button className="btn btn-primary" onClick={load} style={{ marginTop: 12 }}>Refresh</button>
    </div>
  );
}
