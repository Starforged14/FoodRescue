export function formatDate(value?: string): string {
  if (!value) {
    return 'Not available'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return 'Not available'
  }
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

export const fieldClass =
  'mt-1.5 w-full rounded-2xl border border-[#dfe6dc] bg-[#fcfdfb] px-4 py-3 text-sm text-[#24372a] outline-none transition placeholder:text-[#a0aaa1] focus:border-[#71936c] focus:ring-4 focus:ring-[#e8f0e5] disabled:opacity-60'

export const buttonClass =
  'inline-flex items-center justify-center rounded-full bg-[#244c37] px-5 py-2.5 text-sm font-bold text-white transition hover:bg-[#193c2a] disabled:cursor-not-allowed disabled:opacity-55'

export const secondaryButtonClass =
  'inline-flex items-center justify-center rounded-full border border-[#dce4da] bg-white px-4 py-2 text-sm font-semibold text-[#405449] transition hover:bg-[#f6f8f4] disabled:cursor-not-allowed disabled:opacity-55'
