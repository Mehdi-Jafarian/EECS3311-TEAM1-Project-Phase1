import { useState, useEffect } from 'react';
import api from '../../api';

export default function ServiceBrowser({ onBook }) {
  const [services, setServices] = useState([]);

  useEffect(() => {
    api.get('/services').then(res => setServices(res.data)).catch(() => {});
  }, []);

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>Consulting Services</h2>
      <table>
        <thead>
          <tr><th>Name</th><th>Type</th><th>Duration</th><th>Base Price</th><th></th></tr>
        </thead>
        <tbody>
          {services.map(s => (
            <tr key={s.id}>
              <td>{s.name}</td>
              <td>{s.type}</td>
              <td>{s.durationMinutes} min</td>
              <td>${s.basePrice.toFixed(2)}</td>
              <td><button className="btn btn-primary" onClick={onBook}>Book</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
