import { useQuery } from '@tanstack/react-query'
import { equipmentAPI } from '../services/api'

type Filters = { status?: string; type?: string; location_id?: string }

export function useEquipmentList(filters?: Filters) {
  return useQuery({
    queryKey: ['equipment', filters],
    queryFn: () => equipmentAPI.getEquipment(filters)
  })
}
