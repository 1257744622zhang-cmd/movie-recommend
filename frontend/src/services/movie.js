import api from './api'

const movieService = {
  getHotMovies: async () => {
    return await api.get('/movie/hot')
  },
  getMovieById: async (id) => {
    return await api.get(`/movie/${id}`)
  },
  searchMovies: async (keyword) => {
    return await api.get(`/movie/search?keyword=${keyword}`)
  },
  filterMovies: async (filters) => {
    const params = new URLSearchParams()
    if (filters.genre) params.append('genre', filters.genre)
    if (filters.director) params.append('director', filters.director)
    if (filters.actor) params.append('actor', filters.actor)
    return await api.get(`/movie/filter?${params.toString()}`)
  },
  filterMoviesAdvanced: async (filters) => {
    const params = new URLSearchParams()
    if (filters.genre) params.append('genre', filters.genre)
    if (filters.minRating) params.append('minRating', filters.minRating)
    if (filters.startYear) params.append('startYear', filters.startYear)
    if (filters.endYear) params.append('endYear', filters.endYear)
    if (filters.region) params.append('region', filters.region)
    const queryString = params.toString()
    return await api.get(`/movie/filter-advanced${queryString ? '?' + queryString : ''}`)
  }
}

export default movieService
