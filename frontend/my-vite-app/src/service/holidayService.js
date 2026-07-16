import api from './api';

export const getAllHolidaysInBetween = async (startDate, endDate) => {
    const response = await api.get(`/v1/holiday/find-between-dates?startDate=${startDate}&endDate=${endDate}`);
    return response.data;
};
export const getHolidays = async () => {
    const response = await api.get('/v1/holiday/all');
    return response.data;
}
export const getHolidaysByYear = async (year) => {
    const response = await api.get(`/v1/holiday/holiday-by-year?year=${year}`);
    return response.data;
}
export const updateHoliday = async (holiday) => {
    const response = await api.put('/v1/holiday', holiday);
    return response.data;
}

export default { getAllHolidaysInBetween, getHolidays, getHolidaysByYear, updateHoliday };