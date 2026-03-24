import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getProfile, setDefaultList, leaveList } from '../api/user'
import { useAuth } from '../context/AuthContext'

export default function UserPage() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { data: profile, isLoading } = useQuery({ queryKey: ['user'], queryFn: getProfile })

  const setDefault = useMutation({
    mutationFn: (listId: number | null) => setDefaultList(listId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['user'] })
  })

  const leave = useMutation({
    mutationFn: (listId: number) => leaveList(listId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['user'] })
  })

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  if (isLoading || !profile) return <p>Loading...</p>

  return (
    <div>
      <h1>Account</h1>
      <p>{profile.email}</p>
      <button onClick={handleLogout}>Logout</button>

      <h2>Default shopping list</h2>
      <select
        value={profile.defaultListId ?? ''}
        onChange={e => setDefault.mutate(e.target.value ? Number(e.target.value) : null)}
      >
        <option value="">None</option>
        {profile.allLists.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
      </select>

      <h2>Shared lists</h2>
      <ul>
        {profile.sharedLists.map(l => (
          <li key={l.id}>
            {l.name} (by {l.ownerEmail})
            <button onClick={() => leave.mutate(l.id)}>Leave</button>
          </li>
        ))}
      </ul>
    </div>
  )
}
