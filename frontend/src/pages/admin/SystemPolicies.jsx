import { useState, useEffect } from 'react';
import api from '../../api';

export default function SystemPolicies() {
  const [policy, setPolicy] = useState(null);
  const [cancellation, setCancellation] = useState('FREE');
  const [pricing, setPricing] = useState('BASE');
  const [message, setMessage] = useState(null);

  const load = () => {
    api.get('/admin/policy').then(res => {
      setPolicy(res.data);
      if (res.data.cancellationPolicy.includes('Partial')) setCancellation('PARTIAL');
      else if (res.data.cancellationPolicy.includes('No Refund')) setCancellation('NONE');
      else setCancellation('FREE');

      if (res.data.pricingStrategy.includes('Discounted')) setPricing('DISCOUNTED');
      else setPricing('BASE');
    });
  };

  useEffect(() => { load(); }, []);

  const saveCancellation = async () => {
    try {
      await api.put('/admin/policy/cancellation', { policy: cancellation });
      setMessage({ type: 'success', text: 'Cancellation policy updated' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: 'Failed to update' });
    }
  };

  const savePricing = async () => {
    try {
      await api.put('/admin/policy/pricing', { strategy: pricing });
      setMessage({ type: 'success', text: 'Pricing strategy updated' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: 'Failed to update' });
    }
  };

  const toggleNotifications = async () => {
    try {
      await api.put('/admin/policy/notifications', { enabled: !policy.notificationsEnabled });
      setMessage({ type: 'success', text: 'Notifications toggled' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: 'Failed to update' });
    }
  };

  if (!policy) return <p>Loading...</p>;

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>System Policies</h2>
      {message && (
        <div className="card" style={{ background: message.type === 'success' ? '#e8f5e9' : '#ffebee', marginBottom: 16 }}>
          {message.text}
        </div>
      )}

      <div className="card">
        <h3>Current Settings</h3>
        <p><strong>Cancellation Policy:</strong> {policy.cancellationPolicy}</p>
        <p><strong>Pricing Strategy:</strong> {policy.pricingStrategy}</p>
        <p><strong>Notifications:</strong> {policy.notificationsEnabled ? 'Enabled' : 'Disabled'}</p>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: 12 }}>Cancellation Policy</h3>
        <select value={cancellation} onChange={e => setCancellation(e.target.value)}>
          <option value="FREE">Free Cancellation (100% refund)</option>
          <option value="PARTIAL">Partial Refund (50%)</option>
          <option value="NONE">No Refund</option>
        </select>
        <button className="btn btn-primary" onClick={saveCancellation} style={{ marginTop: 8 }}>Save</button>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: 12 }}>Pricing Strategy</h3>
        <select value={pricing} onChange={e => setPricing(e.target.value)}>
          <option value="BASE">Base Pricing (no discount)</option>
          <option value="DISCOUNTED">Discounted Pricing (20% off)</option>
        </select>
        <button className="btn btn-primary" onClick={savePricing} style={{ marginTop: 8 }}>Save</button>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: 12 }}>Notifications</h3>
        <button className={`btn ${policy.notificationsEnabled ? 'btn-danger' : 'btn-success'}`} onClick={toggleNotifications}>
          {policy.notificationsEnabled ? 'Disable Notifications' : 'Enable Notifications'}
        </button>
      </div>
    </div>
  );
}
