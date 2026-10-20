import { Link } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

const roles = [
  {
    number: '01',
    title: 'Share what is extra',
    description: 'Donors can make good food visible before it goes to waste.',
    accent: 'bg-[#e2efdf]',
  },
  {
    number: '02',
    title: 'Find a local match',
    description: 'Community organizations can coordinate a timely pickup.',
    accent: 'bg-[#f5ead6]',
  },
  {
    number: '03',
    title: 'Make the handoff',
    description: 'Volunteers can help move food from a pickup to a drop-off.',
    accent: 'bg-[#e3eaf1]',
  },
]

export function HomePage() {
  const { user } = useAuth()

  return (
    <main>
      <section className="mx-auto grid max-w-7xl items-center gap-12 px-5 pb-20 pt-14 sm:px-8 lg:grid-cols-[1.08fr_0.92fr] lg:gap-16 lg:pb-28 lg:pt-24">
        <div className="max-w-2xl">
          <div className="mb-7 inline-flex items-center gap-2 rounded-full border border-[#dce7d8] bg-white/70 px-3.5 py-2 text-xs font-bold uppercase tracking-[0.13em] text-[#4e7651]">
            <span className="size-2 rounded-full bg-[#79a76e]" />
            A better route for good food
          </div>
          <h1 className="max-w-2xl text-5xl font-semibold leading-[1.06] tracking-[-0.055em] text-[#1d2b24] sm:text-6xl lg:text-[4.45rem]">
            Good food
            <br />
            deserves a
            <span className="font-serif font-medium italic text-[#4e7651]"> second table.</span>
          </h1>
          <p className="mt-7 max-w-xl text-base leading-7 text-[#647268] sm:text-lg sm:leading-8">
            FoodRescue brings local food donors, community organizations, and
            volunteers together to make surplus food easier to share.
          </p>
          <div className="mt-9 flex flex-col gap-3 sm:flex-row">
            <Link
              className="inline-flex items-center justify-center gap-2 rounded-full bg-[#244c37] px-6 py-3.5 text-sm font-bold text-white shadow-md shadow-[#244c37]/15 transition hover:-translate-y-0.5 hover:bg-[#193c2a]"
              to={user ? '/dashboard' : '/register'}
            >
              {user ? 'Open your workspace' : 'Join the food rescue'}
              <span aria-hidden="true">-&gt;</span>
            </Link>
            <Link
              className="inline-flex items-center justify-center rounded-full border border-[#dce4da] bg-white/60 px-6 py-3.5 text-sm font-bold text-[#405449] transition hover:bg-white"
              to={user ? '/dashboard' : '/login'}
            >
              {user ? 'View account' : 'I already have an account'}
            </Link>
          </div>
          <div className="mt-11 flex items-center gap-4 border-t border-[#e2e8df] pt-6">
            <div className="flex -space-x-2">
              {['bg-[#d8e8d4]', 'bg-[#f2ddba]', 'bg-[#dce3ef]'].map((color, index) => (
                <span
                  aria-hidden="true"
                  className={`flex size-9 items-center justify-center rounded-full border-2 border-[#f8f8f4] ${color}`}
                  key={color}
                >
                  <svg
                    aria-hidden="true"
                    className="text-[#536c52]"
                    fill="none"
                    height="17"
                    viewBox="0 0 20 20"
                    width="17"
                  >
                    <circle cx="10" cy="6.5" fill="currentColor" r="3" />
                    <path
                      d="M4.5 17c.3-3 2.4-4.7 5.5-4.7s5.2 1.7 5.5 4.7"
                      fill="currentColor"
                    />
                  </svg>
                  <span className="sr-only">Community member {index + 1}</span>
                </span>
              ))}
            </div>
            <p className="text-xs leading-5 text-[#718075]">
              Built for local action
              <br />
              and real-world handoffs
            </p>
          </div>
        </div>

        <div className="relative mx-auto w-full max-w-[560px]">
          <div className="absolute -right-5 -top-5 size-28 rounded-full bg-[#e9d9b7]/60 blur-2xl" />
          <div className="absolute -bottom-8 -left-5 size-36 rounded-full bg-[#c9ddc6]/80 blur-3xl" />
          <div className="relative overflow-hidden rounded-[2rem] border border-white/80 bg-[#e9eee4] p-4 shadow-[0_28px_80px_-38px_rgba(31,55,38,0.35)] sm:p-6">
            <div className="relative min-h-[390px] overflow-hidden rounded-[1.5rem] bg-[#dce8d4] p-6 sm:min-h-[470px] sm:p-8">
              <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_70%_20%,rgba(255,255,255,0.8),transparent_42%),linear-gradient(145deg,#dce8d4_0%,#c5d7bd_100%)]" />
              <div className="absolute -right-16 top-16 size-64 rounded-full border border-white/30 bg-[#f7f0df]/40" />
              <div className="absolute -right-4 top-28 size-40 rounded-full border border-white/40 bg-[#e7c98f]/60" />
              <div className="absolute bottom-0 left-0 right-0 h-40 bg-[linear-gradient(0deg,rgba(75,107,66,0.2),transparent)]" />
              <div className="relative flex min-h-[342px] flex-col justify-between sm:min-h-[414px]">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#55714e]">A simple idea</p>
                    <p className="mt-2 max-w-[220px] text-2xl font-semibold leading-tight tracking-tight text-[#253d2d]">
                      From extra
                      <br />
                      to essential.
                    </p>
                  </div>
                  <span className="flex size-11 items-center justify-center rounded-full border border-white/70 bg-white/50 text-[#4f744b]">
                    <svg aria-hidden="true" fill="none" height="22" viewBox="0 0 24 24" width="22">
                      <path d="M12 20s-7-4.3-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.7-7 10-7 10Z" stroke="currentColor" strokeWidth="1.7" />
                      <path d="M8.5 12.2h2l1-2.2 1.4 4 1-1.8h1.6" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.4" />
                    </svg>
                  </span>
                </div>

                <div className="relative mx-auto flex h-44 w-full max-w-xs items-end justify-center">
                  <div className="absolute bottom-3 h-5 w-64 rounded-[50%] bg-[#486747]/15 blur-md" />
                  <div className="relative flex h-36 w-52 items-center justify-center rounded-b-[5.5rem] rounded-t-[2.6rem] border-[7px] border-[#fffaf0] bg-[#f3ead1] shadow-[inset_0_-14px_25px_rgba(114,105,70,0.12),0_18px_30px_rgba(71,93,55,0.12)]">
                    <div className="absolute -top-3 h-7 w-48 rounded-[50%] border-[7px] border-[#fffaf0] bg-[#d9e1be]" />
                    <div className="relative -mt-2 flex items-center gap-1">
                      <span className="size-12 rounded-full bg-[#df8e4d] shadow-inner" />
                      <span className="-ml-3 mt-5 size-11 rounded-full bg-[#86a75c] shadow-inner" />
                      <span className="-ml-3 -mt-4 size-12 rounded-full bg-[#e2ba50] shadow-inner" />
                      <span className="-ml-3 mt-4 size-10 rounded-full bg-[#bf6250] shadow-inner" />
                    </div>
                    <span className="absolute left-8 top-8 size-2 rounded-full bg-white/80" />
                    <span className="absolute right-7 top-12 size-1.5 rounded-full bg-white/60" />
                  </div>
                  <div className="absolute bottom-32 left-10 size-12 rounded-full bg-[#668656] shadow-[inset_8px_0_0_rgba(255,255,255,0.12)]" />
                  <div className="absolute bottom-36 left-16 h-8 w-2 rotate-[-35deg] rounded-full bg-[#668656]" />
                  <div className="absolute bottom-32 right-10 size-10 rounded-full bg-[#71945d]" />
                </div>

                <div className="flex items-end justify-between gap-3">
                  <div className="rounded-2xl border border-white/80 bg-white/75 px-4 py-3 shadow-sm backdrop-blur">
                    <p className="text-[10px] font-bold uppercase tracking-[0.14em] text-[#78906f]">The journey</p>
                    <p className="mt-1 text-sm font-semibold text-[#304635]">Donor -&gt; community -&gt; table</p>
                  </div>
                  <div aria-hidden="true" className="mb-1 flex size-12 items-center justify-center rounded-full bg-[#244c37] text-white shadow-lg">
                    <svg fill="none" height="21" viewBox="0 0 24 24" width="21">
                      <path d="M4 12h15m-6-6 6 6-6 6" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" />
                    </svg>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="absolute -bottom-5 -left-3 hidden items-center gap-3 rounded-2xl border border-[#e7ece4] bg-white px-4 py-3 shadow-lg sm:flex">
            <span className="flex size-9 items-center justify-center rounded-xl bg-[#e8f0e2] text-[#527b4f]">
              <svg aria-hidden="true" fill="none" height="19" viewBox="0 0 24 24" width="19">
                <path d="m5 12 4 4L19 6" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" />
              </svg>
            </span>
            <span className="text-xs font-semibold text-[#45574a]">A little less waste, together</span>
          </div>
        </div>
      </section>

      <section className="border-y border-[#e4e9e1] bg-white/55">
        <div className="mx-auto max-w-7xl px-5 py-16 sm:px-8 lg:py-20">
          <div className="mb-9 flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
            <div>
              <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#64805d]">How it comes together</p>
              <h2 className="mt-3 max-w-lg text-3xl font-semibold tracking-[-0.04em] text-[#203328] sm:text-4xl">
                Three roles. One more thoughtful food system.
              </h2>
            </div>
            <p className="max-w-sm text-sm leading-6 text-[#6b796f]">
              Start with the part you can play. FoodRescue helps coordinate the
              details that bring a donation to its destination.
            </p>
          </div>
          <div className="grid gap-4 md:grid-cols-3">
            {roles.map((role) => (
              <article className="rounded-3xl border border-[#e6ebe3] bg-[#fbfcf9] p-6 transition hover:-translate-y-1 hover:shadow-lg hover:shadow-[#244c37]/5" key={role.number}>
                <span className={`flex size-11 items-center justify-center rounded-2xl ${role.accent} text-sm font-bold text-[#4d6a4d}`}>
                  {role.number}
                </span>
                <h3 className="mt-6 text-lg font-semibold text-[#24372a]">{role.title}</h3>
                <p className="mt-2 text-sm leading-6 text-[#718075]">{role.description}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="mx-auto flex max-w-7xl flex-col items-start justify-between gap-7 px-5 py-16 sm:px-8 md:flex-row md:items-center lg:py-20">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#64805d]">Ready when you are</p>
          <h2 className="mt-3 text-3xl font-semibold tracking-[-0.04em] text-[#203328] sm:text-4xl">
            Make your next good move.
          </h2>
        </div>
        <Link
          className="inline-flex items-center gap-3 rounded-full bg-[#244c37] px-6 py-3.5 text-sm font-bold text-white transition hover:bg-[#193c2a]"
          to={user ? '/dashboard' : '/register'}
        >
          {user ? 'Go to workspace' : 'Create your free account'}
          <span aria-hidden="true">-&gt;</span>
        </Link>
      </section>
    </main>
  )
}
