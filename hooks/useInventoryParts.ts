import { useQuery } from '@tanstack/react-query'
import { inventoryAPI } from '../services/api'

export function useInventoryParts(filters?: { category?: string; low_stock?: boolean }) {
  return useQuery({
    queryKey: ['inventory','parts', filters],
    queryFn: () => inventoryAPI.getParts(filters)
  })
}
