import { Link } from 'react-router-dom'

export function Brand() {
  return (
    <Link aria-label="FoodRescue home" className="group flex items-center gap-2.5" to="/">
      <span className="flex size-10 items-center justify-center rounded-2xl bg-[#244c37] text-white shadow-sm transition group-hover:-rotate-3">
        <svg aria-hidden="true" fill="none" height="22" viewBox="0 0 24 24" width="22">
          <path
            d="M19.8 4.2C12.7 4.2 7.1 6.2 5.3 11c-1.1 2.9.3 5.8 3.1 6.2 4.3.7 8.8-4.7 11.4-13Z"
            fill="currentColor"
            opacity=".9"
          />
          <path d="M4.3 20c2.5-5.7 6.7-9.3 11.5-11.8" stroke="#b8d9a8" strokeLinecap="round" strokeWidth="1.8" />
        </svg>
      </span>
      <span className="text-lg font-bold tracking-tight text-[#1d2b24]">
        food<span className="text-[#4d7c53]">rescue</span>
      </span>
    </Link>
  )
}
