import { useState } from 'react';
import api from '../api';

export default function RoleSelect({ onSelect }) {
  const [step, setStep] = useState('role');
  const [role, setRole] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [regName, setRegName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regMsg, setRegMsg] = useState(null);

  const pickRole = async (r) => {
    if (r === 'admin') {
      onSelect({ role: 'admin' });
      return;
    }
    setRole(r);
    setLoading(true);
    setShowRegister(false);
    setRegMsg(null);
    try {
      const endpoint = r === 'client' ? '/clients' : '/consultants';
      const res = await api.get(endpoint);
      setUsers(res.data);
    } catch (e) {
      alert('Failed to load users');
    }
    setLoading(false);
    setStep('user');
  };

  const register = async () => {
    if (!regName.trim() || !regEmail.trim()) return;
    try {
      const endpoint = role === 'client' ? '/clients' : '/consultants';
      const res = await api.post(endpoint, { name: regName.trim(), email: regEmail.trim() });
      if (role === 'consultant') {
        setRegMsg({ type: 'success', text: `Registered! Your status is PENDING — an admin must approve you before you can log in.` });
      } else {
        setRegMsg({ type: 'success', text: `Registered successfully! You can now select your account below.` });
      }
      setRegName('');
      setRegEmail('');
      // Refresh the user list
      const refreshed = await api.get(endpoint);
      setUsers(refreshed.data);
    } catch (e) {
      setRegMsg({ type: 'error', text: e.response?.data?.error || 'Registration failed' });
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f5f5f5' }}>
      <div style={{ background: '#fff', borderRadius: 12, padding: 40, boxShadow: '0 4px 12px rgba(0,0,0,0.15)', minWidth: 420, maxWidth: 480, textAlign: 'center' }}>
        <h1 style={{ marginBottom: 8, color: '#1976d2' }}>Service Booking Platform</h1>
        <p style={{ color: '#666', marginBottom: 32 }}>Select your role to continue</p>

        {step === 'role' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            <button className="btn btn-primary" style={{ padding: 14, fontSize: 16 }} onClick={() => pickRole('client')}>
              Client
            </button>
            <button className="btn btn-success" style={{ padding: 14, fontSize: 16 }} onClick={() => pickRole('consultant')}>
              Consultant
            </button>
            <button className="btn btn-warning" style={{ padding: 14, fontSize: 16 }} onClick={() => pickRole('admin')}>
              Admin
            </button>
          </div>
        )}

        {step === 'user' && (
          <div>
            <h3 style={{ marginBottom: 16 }}>
              {showRegister ? `Register as ${role}` : `Select ${role}`}
            </h3>

            {regMsg && (
              <div style={{
                background: regMsg.type === 'success' ? '#e8f5e9' : '#ffebee',
                padding: '10px 14px', borderRadius: 8, marginBottom: 12, fontSize: 14, textAlign: 'left'
              }}>
                {regMsg.text}
              </div>
            )}

            {!showRegister ? (
              <>
                {loading ? <p>Loading...</p> : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {users.map(u => (
                      <button key={u.id} className="btn" style={{ background: '#e3f2fd', color: '#1976d2', padding: 12, fontSize: 14 }}
                        onClick={() => onSelect({ ...u, role })}>
                        {u.name} ({u.email})
                        {role === 'consultant' && u.status && (
                          <span style={{
                            marginLeft: 8, fontSize: 11, padding: '2px 8px', borderRadius: 10, color: '#fff',
                            background: u.status === 'APPROVED' ? '#4caf50' : u.status === 'PENDING' ? '#ff9800' : '#f44336'
                          }}>
                            {u.status}
                          </span>
                        )}
                      </button>
                    ))}
                    <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
                      <button className="btn" style={{ flex: 1 }} onClick={() => { setStep('role'); setRole(null); setRegMsg(null); }}>
                        Back
                      </button>
                      <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => setShowRegister(true)}>
                        Register New
                      </button>
                    </div>
                  </div>
                )}
              </>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10, textAlign: 'left' }}>
                <label style={{ fontWeight: 600, fontSize: 14 }}>Name</label>
                <input placeholder="Full name" value={regName} onChange={e => setRegName(e.target.value)} />
                <label style={{ fontWeight: 600, fontSize: 14 }}>Email</label>
                <input placeholder="email@example.com" value={regEmail} onChange={e => setRegEmail(e.target.value)} />
                {role === 'consultant' && (
                  <p style={{ fontSize: 12, color: '#888', margin: 0 }}>
                    New consultants start with PENDING status. An admin must approve you before you can manage bookings.
                  </p>
                )}
                <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                  <button className="btn" style={{ flex: 1 }} onClick={() => { setShowRegister(false); setRegMsg(null); }}>
                    Cancel
                  </button>
                  <button className="btn btn-success" style={{ flex: 1 }} onClick={register}
                    disabled={!regName.trim() || !regEmail.trim()}>
                    Register
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
