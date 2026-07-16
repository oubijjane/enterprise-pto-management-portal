import { useState, useEffect } from 'react';
import { HolidaysView } from '../components/Views'; // Or wherever you saved it
import holidayService from '../service/holidayService';

export default function HolidaysPage() {
  const [holidays, setHolidays] = useState([]);
  const [loading, setLoading] = useState(true);

  // Load Data
  useEffect(() => {
    const loadData = async () => {
      try {
        const data = await holidayService.getHolidaysByYear(new Date().getFullYear());
        // setHolidays(data);
        
        // Mock data for testing
        setHolidays(data);
      } catch (error) {
        console.error("Failed to load holidays:", error);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  // Handle Add & Update
  const handleSave = async (holidayData) => {
    if (holidayData.id) {
      // It's an update
      await holidayService.updateHoliday(holidayData);
      setHolidays(prev => prev.map(h => h.id === holidayData.id ? holidayData : h));
    } else {
      // It's a new holiday
      // const newHoliday = await addHoliday(holidayData);
      const newHoliday = { ...holidayData, id: Date.now() }; // Mock ID
      setHolidays(prev => [...prev, newHoliday].sort((a, b) => new Date(a.date) - new Date(b.date)));
    }
  };

  // Handle Delete
  const handleDelete = async (id) => {
    // await deleteHoliday(id);
    setHolidays(prev => prev.filter(h => h.id !== id));
  };

  if (loading) return <div>Loading holidays...</div>;

  return (
    <HolidaysView 
      holidays={holidays} 
      onSave={handleSave} 
      onDelete={handleDelete} 
    />
  );
}