import api from './api'

const dislikeMovie = async (movieId) => {
  return await api.post(`/recommend-feedback/dislike?movieId=${movieId}`)
}

const cancelDislike = async (movieId) => {
  return await api.delete(`/recommend-feedback/dislike?movieId=${movieId}`)
}

const getDislikedMovies = async () => {
  return await api.get('/recommend-feedback/disliked')
}

export { dislikeMovie, cancelDislike, getDislikedMovies }
