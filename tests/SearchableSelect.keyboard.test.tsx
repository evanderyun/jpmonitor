import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SearchableSelect from '../components/SearchableSelect'

describe('SearchableSelect keyboard', () => {
  it('navigates and selects with keyboard', async () => {
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
    await user.keyboard('{ArrowDown}{Enter}')
    expect(onChange).toHaveBeenCalledWith('a')
  })
})
