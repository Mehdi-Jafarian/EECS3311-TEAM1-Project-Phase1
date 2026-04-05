import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { useState } from 'react';
import RoleSelect from './pages/RoleSelect';
import ClientDashboard from './pages/client/ClientDashboard';
import ConsultantDashboard from './pages/consultant/ConsultantDashboard';
import AdminDashboard from './pages/admin/AdminDashboard';

export default function App() {
  const [user, setUser] = useState(null);

  if (!user) {
    return <RoleSelect onSelect={setUser} />;
  }

  return (
    <BrowserRouter>
      <div style={{ minHeight: '100vh', background: '#f5f5f5' }}>
        <nav style={{ background: '#1976d2', color: '#fff', padding: '12px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <strong>Service Booking Platform</strong>
          <div>
            <span style={{ marginRight: 16 }}>
              {user.role === 'admin' ? 'Admin' : `${user.name} (${user.role})`}
            </span>
            <button onClick={() => setUser(null)} style={{ background: '#fff', color: '#1976d2', border: 'none', borderRadius: 4, padding: '6px 12px', cursor: 'pointer' }}>
              Logout
            </button>
          </div>
        </nav>
        <div style={{ padding: 24, maxWidth: 1200, margin: '0 auto' }}>
          <Routes>
            <Route path="/*" element={
              user.role === 'client' ? <ClientDashboard user={user} /> :
              user.role === 'consultant' ? <ConsultantDashboard user={user} /> :
              <AdminDashboard />
            } />
          </Routes>
        </div>
      </div>
    </BrowserRouter>
  );
}
