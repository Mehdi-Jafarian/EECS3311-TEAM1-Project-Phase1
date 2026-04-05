import { useState } from 'react';
import ConsultantApproval from './ConsultantApproval';
import SystemPolicies from './SystemPolicies';
import SystemStatus from './SystemStatus';

export default function AdminDashboard() {
  const [tab, setTab] = useState('approval');

  const tabs = [
    { id: 'approval', label: 'Consultant Approval' },
    { id: 'policies', label: 'System Policies' },
    { id: 'status', label: 'System Status' },
  ];

  return (
    <div>
      <div className="tabs">
        {tabs.map(t => (
          <button key={t.id} className={`tab ${tab === t.id ? 'active' : ''}`} onClick={() => setTab(t.id)}>
            {t.label}
          </button>
        ))}
      </div>
      {tab === 'approval' && <ConsultantApproval />}
      {tab === 'policies' && <SystemPolicies />}
      {tab === 'status' && <SystemStatus />}
    </div>
  );
}
