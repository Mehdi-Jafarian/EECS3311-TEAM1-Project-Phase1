import { useState, useEffect, useCallback } from 'react';
import api from '../../api';

export default function SystemStatus() {
  const [consultants, setConsultants] = useState([]);
  const [message, setMessage] = useState(null);

  const load = useCallback(() => {
    api.get('/consultants').then(res => setConsultants(res.data));
  }, []);

  useEffect(() => { load(); }, [load]);

  const approve = async (id) => {
    try {
      await api.put(`/admin/consultants/${id}/approve`);
      setMessage({ type: 'success', text: 'Consultant approved' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Failed to approve' });
    }
  };

  const reject = async (id) => {
    if (!confirm('Are you sure you want to revoke/reject this consultant?')) return;
    try {
      await api.put(`/admin/consultants/${id}/reject`);
      setMessage({ type: 'success', text: 'Consultant status set to REJECTED' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Failed to reject' });
    }
  };

  const statusColor = (status) => {
    switch (status) {
      case 'APPROVED': return '#4caf50';
      case 'PENDING': return '#ff9800';
      case 'REJECTED': return '#f44336';
      default: return '#9e9e9e';
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>System Status</h2>

      {message && (
        <div className="card" style={{ background: message.type === 'success' ? '#e8f5e9' : '#ffebee', marginBottom: 16 }}>
          {message.text}
          <button onClick={() => setMessage(null)} style={{ float: 'right', border: 'none', background: 'none', cursor: 'pointer' }}>✕</button>
        </div>
      )}

      <div className="card">
        <h3 style={{ marginBottom: 12 }}>All Consultants</h3>
        <table>
          <thead>
            <tr><th>Name</th><th>Email</th><th>Status</th><th>Actions</th></tr>
          </thead>
          <tbody>
            {consultants.map(c => (
              <tr key={c.id}>
                <td>{c.name}</td>
                <td>{c.email}</td>
                <td>
                  <span style={{
                    padding: '4px 10px', borderRadius: 12, fontSize: 12,
                    fontWeight: 'bold', color: '#fff', background: statusColor(c.status)
                  }}>
                    {c.status}
                  </span>
                </td>
                <td>
                  {c.status !== 'APPROVED' && (
                    <button className="btn btn-success" onClick={() => approve(c.id)}>Approve</button>
                  )}
                  {c.status !== 'REJECTED' && (
                    <button className="btn btn-danger" onClick={() => reject(c.id)}>
                      {c.status === 'APPROVED' ? 'Revoke' : 'Reject'}
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {consultants.length === 0 && (
              <tr><td colSpan={4} style={{ textAlign: 'center', color: '#999' }}>No consultants</td></tr>
            )}
          </tbody>
        </table>
      </div>

      <button className="btn btn-primary" onClick={load} style={{ marginTop: 12 }}>Refresh</button>
    </div>
  );
}
