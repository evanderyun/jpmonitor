import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SearchableSelect from '../components/SearchableSelect'

describe('SearchableSelect', () => {
  it('opens and selects an option', async () => {
    const user = userEvent.setup()
    const options = [
      { value: 'a', label: 'Alpha' },
      { value: 'b', label: 'Beta' }
    ]
    const onChange = vi.fn()
    render(<SearchableSelect options={options} value="" onChange={onChange} />)
    const combobox = screen.getByRole('combobox')
    combobox.focus()
    await user.keyboard('{Enter}')
    await screen.findByRole('listbox')
    const opt = screen.getByRole('option', { name: 'Alpha' })
    await user.click(opt)
    expect(onChange).toHaveBeenCalledWith('a')
  })
})
