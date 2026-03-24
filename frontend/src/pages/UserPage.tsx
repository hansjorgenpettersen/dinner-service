import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getProfile, setDefaultList, leaveList } from '../api/user'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card'

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

  if (isLoading || !profile) return <div className="max-w-4xl mx-auto px-4 py-8 text-[#7a5c3a]">Loading…</div>

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-[#3d1f08] mb-6">Account</h1>

      <Card className="border-[#e8c9a0] shadow-sm mb-6">
        <CardHeader className="pb-2">
          <CardTitle className="text-base text-[#3d1f08]">Profile</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-[#7a5c3a]">{profile.email}</p>
        </CardContent>
      </Card>

      <Card className="border-[#e8c9a0] shadow-sm mb-6">
        <CardHeader className="pb-2">
          <CardTitle className="text-base text-[#3d1f08]">Default Shopping List</CardTitle>
        </CardHeader>
        <CardContent>
          <select
            value={profile.defaultListId ?? ''}
            onChange={e => setDefault.mutate(e.target.value ? Number(e.target.value) : null)}
            className="border border-[#e8c9a0] rounded-md px-3 py-2 text-sm bg-white text-[#3d1f08] w-full"
          >
            <option value="">None</option>
            {profile.allLists.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
          </select>
        </CardContent>
      </Card>

      {profile.sharedLists.length > 0 && (
        <Card className="border-[#e8c9a0] shadow-sm mb-6">
          <CardHeader className="pb-2">
            <CardTitle className="text-base text-[#3d1f08]">Shared Lists</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-2 p-0">
            {profile.sharedLists.map(l => (
              <div key={l.id} className="flex items-center gap-3 px-6 py-3 border-b border-[#f5ebe0] last:border-0">
                <div className="flex-1">
                  <p className="text-sm font-medium text-[#3d1f08]">{l.name}</p>
                  <p className="text-xs text-[#7a5c3a]">by {l.ownerEmail}</p>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => leave.mutate(l.id)}
                  className="border-[#e8c9a0] text-[#7a5c3a] hover:border-red-300 hover:text-red-600"
                >
                  Leave
                </Button>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      <Button
        variant="destructive"
        onClick={handleLogout}
        className="w-full"
      >
        Sign out
      </Button>
    </div>
  )
}
