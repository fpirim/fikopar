import { useLocation } from 'react-router-dom'

export default function LocationProbe() {
  const { pathname, search } = useLocation()
  return <output data-testid="location">{pathname + search}</output>
}
