import api from './api'

const adminService = {
  getAllMovies: async () => {
    return await api.get('/movie/all')
  },
  addMovie: async (movie) => {
    return await api.post('/movie', movie)
  },
  updateMovie: async (id, movie) => {
    return await api.put(`/movie/${id}`, movie)
  },
  deleteMovie: async (id) => {
    return await api.delete(`/movie/${id}`)
  }
}

export default adminService
