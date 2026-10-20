import { useState } from 'react'
import { AdminDashboard } from './dashboards/AdminDashboard'
import { DonorDashboard } from './dashboards/DonorDashboard'
import { NgoDashboard } from './dashboards/NgoDashboard'
import { VolunteerDashboard } from './dashboards/VolunteerDashboard'
import { useAuth } from '../context/useAuth'
import type { AppRole } from '../types/auth'

const roleLabels: Record<AppRole, string> = {
  DONOR: 'Donor workspace',
  NGO: 'NGO workspace',
  VOLUNTEER: 'Volunteer workspace',
  ADMIN: 'Admin workspace',
}

export function DashboardPage() {
  const { user } = useAuth()
  const userRoles = user?.roles ?? []
  const initialRole: AppRole = userRoles[0] ?? 'DONOR'
  const [selectedRole, setSelectedRole] = useState<AppRole>(initialRole)
  const activeRole = userRoles.includes(selectedRole) ? selectedRole : (userRoles[0] ?? 'DONOR')

  return (
    <main className="mx-auto min-h-[70vh] max-w-7xl px-5 py-10 sm:px-8 lg:py-14">
      <div className="mb-8 flex flex-col justify-between gap-5 border-b border-[#e4e9e1] pb-6 sm:flex-row sm:items-end">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#64805d]">FoodRescue workspace</p>
          <h1 className="mt-2 text-3xl font-semibold tracking-[-0.045em] text-[#203328] sm:text-4xl">
            {roleLabels[activeRole]}
          </h1>
          <p className="mt-2 text-sm text-[#718075]">{user?.email}</p>
        </div>
        {userRoles.length > 1 && (
          <label className="block text-xs font-semibold text-[#718075]">
            Switch workspace
            <select
              className="mt-1.5 min-w-48 rounded-xl border border-[#dfe6dc] bg-white px-3 py-2 text-sm font-semibold text-[#34473a] outline-none focus:border-[#71936c]"
              onChange={(event) => setSelectedRole(event.target.value as AppRole)}
              value={activeRole}
            >
              {userRoles.map((role) => (
                <option key={role} value={role}>{roleLabels[role]}</option>
              ))}
            </select>
          </label>
        )}
      </div>

      {activeRole === 'DONOR' && <DonorDashboard />}
      {activeRole === 'NGO' && <NgoDashboard />}
      {activeRole === 'VOLUNTEER' && <VolunteerDashboard />}
      {activeRole === 'ADMIN' && <AdminDashboard />}
    </main>
  )
}
