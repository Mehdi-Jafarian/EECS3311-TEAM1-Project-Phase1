import { useState } from 'react';
import Availability from './Availability';
import BookingRequests from './BookingRequests';
import BookingSchedule from './BookingSchedule';

export default function ConsultantDashboard({ user }) {
  const [tab, setTab] = useState('availability');

  const tabs = [
    { id: 'availability', label: 'Availability' },
    { id: 'requests', label: 'Booking Requests' },
    { id: 'schedule', label: 'Booking Schedule' },
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
      {tab === 'availability' && <Availability consultantId={user.id} />}
      {tab === 'requests' && <BookingRequests consultantId={user.id} />}
      {tab === 'schedule' && <BookingSchedule consultantId={user.id} />}
    </div>
  );
}
