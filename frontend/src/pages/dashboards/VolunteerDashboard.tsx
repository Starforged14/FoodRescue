import axios from 'axios'
import { useCallback, useEffect, useState, type FormEvent, type ReactNode } from 'react'
import {
  DashboardTitle,
  EmptyState,
  Feedback,
  Panel,
  StatusBadge,
} from '../../components/DashboardUI'
import { buttonClass, fieldClass, formatDate, secondaryButtonClass } from '../../components/dashboard-styles'
import { api } from '../../lib/api'
import { getErrorMessage } from '../../lib/error-message'
import type {
  DeliveryTask,
  DeliveryTaskSummary,
  Page,
  VolunteerAvailability,
  VolunteerProfile,
  VolunteerProfileRequest,
} from '../../types/platform'

const availabilityOptions: VolunteerAvailability[] = ['AVAILABLE', 'UNAVAILABLE']

export function VolunteerDashboard() {
  const [profile, setProfile] = useState<VolunteerProfile | null>(null)
  const [availabilityStatus, setAvailabilityStatus] = useState<VolunteerAvailability>('AVAILABLE')
  const [maxTravelDistanceKm, setMaxTravelDistanceKm] = useState('')
  const [openTasks, setOpenTasks] = useState<DeliveryTaskSummary[]>([])
  const [myTasks, setMyTasks] = useState<DeliveryTaskSummary[]>([])
  const [selectedTask, setSelectedTask] = useState<DeliveryTask | null>(null)
  const [failureReason, setFailureReason] = useState('')
  const [loading, setLoading] = useState(true)
  const [savingProfile, setSavingProfile] = useState(false)
  const [busyTaskId, setBusyTaskId] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const loadDashboard = useCallback(async () => {
    const profileRequest = api.get<VolunteerProfile>('/volunteer/profile').then((response) => {
      setProfile(response.data)
      setAvailabilityStatus(response.data.availabilityStatus)
      setMaxTravelDistanceKm(response.data.maxTravelDistanceKm?.toString() ?? '')
    }).catch((requestError: unknown) => {
      if (axios.isAxiosError(requestError) && requestError.response?.status === 404) {
        setProfile(null)
        return
      }
      throw requestError
    })
    const openRequest = api.get<Page<DeliveryTaskSummary>>('/volunteer/deliveries/open', {
      params: { page: 0, size: 20, sort: 'pickupWindowStart,asc' },
    }).then((response) => setOpenTasks(response.data?.content ?? []))
    const mineRequest = api.get<Page<DeliveryTaskSummary>>('/volunteer/deliveries/mine', {
      params: { page: 0, size: 20, sort: 'createdAt,desc' },
    }).then((response) => setMyTasks(response.data?.content ?? []))

    try {
      await Promise.all([profileRequest, openRequest, mineRequest])
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    queueMicrotask(() => void loadDashboard())
  }, [loadDashboard])

  async function saveProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSavingProfile(true)
    setError('')
    setMessage('')
    const request: VolunteerProfileRequest = {
      availabilityStatus,
      maxTravelDistanceKm: maxTravelDistanceKm ? Number(maxTravelDistanceKm) : undefined,
    }
    try {
      const response = await api.put<VolunteerProfile>('/volunteer/profile', request)
      setProfile(response.data)
      setMessage('Volunteer profile saved.')
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSavingProfile(false)
    }
  }

  async function acceptTask(task: DeliveryTaskSummary) {
    setBusyTaskId(task.id)
    setError('')
    setMessage('')
    try {
      const response = await api.post<DeliveryTask>(`/volunteer/deliveries/${task.id}/accept`)
      setSelectedTask(response.data)
      setMessage('Delivery accepted. Pickup and drop-off addresses are now available in your task details.')
      await loadDashboard()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setBusyTaskId(null)
    }
  }

  async function openTask(task: DeliveryTaskSummary) {
    setError('')
    try {
      const response = await api.get<DeliveryTask>(`/volunteer/deliveries/${task.id}`)
      setSelectedTask(response.data)
      setFailureReason('')
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    }
  }

  async function updateTask(action: 'pickup' | 'delivered' | 'fail' | 'cancel') {
    if (!selectedTask) {
      return
    }
    if (action === 'fail' && !failureReason.trim()) {
      setError('Add a short reason before marking a delivery as failed.')
      return
    }
    setBusyTaskId(selectedTask.id)
    setError('')
    setMessage('')
    try {
      const body = action === 'fail' ? { reason: failureReason.trim() } : undefined
      const response = await api.post<DeliveryTask>(
        `/volunteer/deliveries/${selectedTask.id}/${action}`,
        body,
      )
      setSelectedTask(response.data)
      setFailureReason('')
      setMessage(
        action === 'pickup'
          ? 'Pickup recorded.'
          : action === 'delivered'
            ? 'Delivery completed.'
            : action === 'fail'
              ? 'Delivery marked as failed.'
              : 'Assignment cancelled.',
      )
      await loadDashboard()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setBusyTaskId(null)
    }
  }

  const canTakeTask = profile?.availabilityStatus === 'AVAILABLE'

  return (
    <>
      <DashboardTitle
        description="Set your availability, find available open delivery tasks, and keep each handoff up to date."
        eyebrow="Volunteer"
        title="Help food find its way."
      />
      <Feedback error={error} message={message} onDismiss={() => { setError(''); setMessage('') }} />
      <div className="grid items-start gap-6 xl:grid-cols-[0.72fr_1.28fr]">
        <div className="space-y-6">
          <Panel
            aside={profile ? <StatusBadge status={profile.availabilityStatus} /> : undefined}
            title={profile ? 'Volunteer profile' : 'Set up your volunteer profile'}
          >
            {!profile && (
              <p className="mb-4 text-sm leading-6 text-[#718075]">
                Create a profile before accepting a delivery. Your availability can be updated when you are not assigned to an active task.
              </p>
            )}
            <form className="space-y-4" onSubmit={saveProfile}>
              <FormField label="Availability">
                <select
                  className={fieldClass}
                  onChange={(event) => setAvailabilityStatus(event.target.value as VolunteerAvailability)}
                  value={availabilityStatus}
                >
                  {availabilityOptions.map((option) => (
                    <option key={option} value={option}>{formatEnum(option)}</option>
                  ))}
                </select>
              </FormField>
              <FormField label="Maximum travel distance in km (optional)">
                <input
                  className={fieldClass}
                  max="9999.99"
                  min="0.1"
                  onChange={(event) => setMaxTravelDistanceKm(event.target.value)}
                  step="0.1"
                  type="number"
                  value={maxTravelDistanceKm}
                />
              </FormField>
              <button className={buttonClass} disabled={savingProfile} type="submit">
                {savingProfile ? 'Saving...' : profile ? 'Update profile' : 'Save profile'}
              </button>
            </form>
          </Panel>

          <Panel title="My delivery tasks">
            {loading ? (
              <LoadingState />
            ) : myTasks.length === 0 ? (
              <EmptyState
                description="Accepted deliveries and their current progress will appear here."
                title="No assigned tasks"
              />
            ) : (
              <div className="space-y-2">
                {myTasks.map((task) => (
                  <button
                    className={`flex w-full items-center justify-between gap-3 rounded-2xl border px-4 py-3 text-left transition ${
                      selectedTask?.id === task.id
                        ? 'border-[#86a881] bg-[#f1f7ee]'
                        : 'border-[#e8ede6] bg-[#fcfdfb] hover:border-[#cbd8c8]'
                    }`}
                    key={task.id}
                    onClick={() => void openTask(task)}
                    type="button"
                  >
                    <span>
                      <span className="block text-sm font-semibold text-[#35483a]">{task.foodTitle}</span>
                      <span className="mt-1 block text-xs text-[#839087]">{task.pickupCity} to {task.dropoffCity}</span>
                    </span>
                    <StatusBadge status={task.status} />
                  </button>
                ))}
              </div>
            )}
          </Panel>
        </div>

        <div className="space-y-6">
          {selectedTask && (
            <Panel
              aside={<StatusBadge status={selectedTask.status} />}
              title={selectedTask.foodTitle}
            >
              <div className="grid gap-3 md:grid-cols-2">
                <AddressCard label="Pickup" address={selectedTask.pickupAddress} />
                <AddressCard label="Drop-off" address={selectedTask.dropoffAddress} />
              </div>
              <div className="mt-4 grid gap-2 rounded-2xl bg-[#f7f9f5] p-4 text-xs text-[#718075] sm:grid-cols-2">
                <p>Pickup window starts: {formatDate(selectedTask.pickupWindowStart)}</p>
                <p>Pickup window ends: {formatDate(selectedTask.pickupWindowEnd)}</p>
                {selectedTask.failureReason && <p className="sm:col-span-2">Reason: {selectedTask.failureReason}</p>}
              </div>
              <div className="mt-4 flex flex-wrap gap-2">
                {selectedTask.status === 'ASSIGNED' && (
                  <>
                    <button className={buttonClass} disabled={busyTaskId === selectedTask.id} onClick={() => void updateTask('pickup')} type="button">
                      Mark picked up
                    </button>
                    <button className={secondaryButtonClass} disabled={busyTaskId === selectedTask.id} onClick={() => void updateTask('cancel')} type="button">
                      Cancel assignment
                    </button>
                    <FailureAction
                      busy={busyTaskId === selectedTask.id}
                      onFailure={setFailureReason}
                      onSubmit={() => void updateTask('fail')}
                      reason={failureReason}
                    />
                  </>
                )}
                {selectedTask.status === 'PICKED_UP' && (
                  <>
                    <button className={buttonClass} disabled={busyTaskId === selectedTask.id} onClick={() => void updateTask('delivered')} type="button">
                      Mark delivered
                    </button>
                    <FailureAction
                      busy={busyTaskId === selectedTask.id}
                      onFailure={setFailureReason}
                      onSubmit={() => void updateTask('fail')}
                      reason={failureReason}
                    />
                  </>
                )}
              </div>
            </Panel>
          )}

          <Panel
            aside={<span className="text-xs text-[#879288]">{openTasks.length} open</span>}
            title="Open delivery queue"
          >
            {loading ? (
              <LoadingState />
            ) : openTasks.length === 0 ? (
              <EmptyState
                description="There are no open tasks at the moment. Check back later for a local delivery."
                title="Queue is clear"
              />
            ) : (
              <div className="space-y-3">
                {openTasks.map((task) => (
                  <article className="rounded-2xl border border-[#e8ede6] bg-[#fcfdfb] p-4 sm:p-5" key={task.id}>
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <h3 className="font-semibold text-[#304635]">{task.foodTitle}</h3>
                        <p className="mt-1 text-sm text-[#718075]">
                          {task.pickupCity}{task.pickupRegion ? `, ${task.pickupRegion}` : ''} to {task.dropoffCity}{task.dropoffRegion ? `, ${task.dropoffRegion}` : ''}
                        </p>
                      </div>
                      <StatusBadge status={task.status} />
                    </div>
                    <div className="mt-3 grid gap-2 text-xs text-[#718075] sm:grid-cols-2">
                      <p>Window starts: {formatDate(task.pickupWindowStart)}</p>
                      <p>Window ends: {formatDate(task.pickupWindowEnd)}</p>
                    </div>
                    <button
                      className={`${buttonClass} mt-4`}
                      disabled={!canTakeTask || busyTaskId === task.id}
                      onClick={() => void acceptTask(task)}
                      type="button"
                    >
                      {!profile ? 'Set up profile to accept' : !canTakeTask ? 'Set availability to accept' : busyTaskId === task.id ? 'Accepting...' : 'Accept delivery'}
                    </button>
                  </article>
                ))}
              </div>
            )}
          </Panel>
        </div>
      </div>
    </>
  )
}

function AddressCard({
  address,
  label,
}: {
  address: DeliveryTask['pickupAddress']
  label: string
}) {
  return (
    <div className="rounded-2xl border border-[#e8ede6] bg-[#fcfdfb] p-4">
      <p className="text-[11px] font-bold uppercase tracking-[0.12em] text-[#718075]">{label}</p>
      <address className="mt-2 not-italic text-sm leading-6 text-[#35483a]">
        {address.addressLine1}
        {address.addressLine2 && <><br />{address.addressLine2}</>}
        <br />
        {address.city}{address.region ? `, ${address.region}` : ''} {address.postalCode}
        <br />
        {address.countryCode}
      </address>
      {address.pickupInstructions && (
        <p className="mt-3 text-xs leading-5 text-[#718075]">
          Instructions: {address.pickupInstructions}
        </p>
      )}
    </div>
  )
}

function FailureAction({
  busy,
  onFailure,
  onSubmit,
  reason,
}: {
  busy: boolean
  onFailure: (value: string) => void
  onSubmit: () => void
  reason: string
}) {
  return (
    <div className="flex min-w-full flex-col gap-2 sm:min-w-72 sm:flex-row">
      <input
        aria-label="Reason delivery cannot be completed"
        className={`${fieldClass} mt-0 min-w-0 flex-1`}
        maxLength={2000}
        onChange={(event) => onFailure(event.target.value)}
        placeholder="Reason if delivery cannot be completed"
        value={reason}
      />
      <button className={secondaryButtonClass} disabled={busy} onClick={onSubmit} type="button">
        Report issue
      </button>
    </div>
  )
}

function FormField({ children, label }: { children: ReactNode; label: string }) {
  return (
    <label className="block text-xs font-semibold text-[#526358]">
      {label}
      {children}
    </label>
  )
}

function LoadingState() {
  return <p className="py-8 text-center text-sm text-[#718075]">Loading delivery tasks...</p>
}

function formatEnum(value: string): string {
  return value.toLowerCase().replaceAll('_', ' ')
}
