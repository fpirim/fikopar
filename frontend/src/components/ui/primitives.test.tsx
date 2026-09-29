import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Button } from './button'
import { Input } from './input'
import { Label } from './label'
import { Checkbox } from './checkbox'
import { Tabs, TabsContent, TabsList, TabsTrigger } from './tabs'
import { Dialog, DialogContent, DialogDescription, DialogTitle, DialogTrigger } from './dialog'
import { Popover, PopoverContent, PopoverTrigger } from './popover'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './select'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from './dropdown-menu'
import { Command, CommandEmpty, CommandInput, CommandItem, CommandList } from './command'
import { Toaster } from './sonner'

describe('ui primitives', () => {
  it('button merges variant classes and handles clicks', async () => {
    const onClick = vi.fn()
    render(
      <Button variant="outline" size="sm" className="w-24" onClick={onClick}>
        Save
      </Button>,
    )
    const btn = screen.getByRole('button', { name: 'Save' })
    expect(btn).toHaveClass('border-input', 'h-[30px]', 'w-24')
    await userEvent.click(btn)
    expect(onClick).toHaveBeenCalledOnce()
  })

  it('label, input and checkbox are wired together', async () => {
    render(
      <>
        <Label htmlFor="code">Code</Label>
        <Input id="code" />
        <Checkbox aria-label="only" />
      </>,
    )
    await userEvent.type(screen.getByLabelText('Code'), 'SF-1')
    expect(screen.getByLabelText('Code')).toHaveValue('SF-1')
    const box = screen.getByRole('checkbox', { name: 'only' })
    await userEvent.click(box)
    expect(box).toHaveAttribute('data-state', 'checked')
  })

  it('tabs switch content', async () => {
    render(
      <Tabs defaultValue="a">
        <TabsList>
          <TabsTrigger value="a">A</TabsTrigger>
          <TabsTrigger value="b">B</TabsTrigger>
        </TabsList>
        <TabsContent value="a">panel a</TabsContent>
        <TabsContent value="b">panel b</TabsContent>
      </Tabs>,
    )
    expect(screen.getByText('panel a')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('tab', { name: 'B' }))
    expect(screen.getByText('panel b')).toBeInTheDocument()
  })

  it('dialog and popover open from their triggers', async () => {
    render(
      <>
        <Dialog>
          <DialogTrigger>open dialog</DialogTrigger>
          <DialogContent>
            <DialogTitle>Post</DialogTitle>
            <DialogDescription>Post the invoice?</DialogDescription>
          </DialogContent>
        </Dialog>
        <Popover>
          <PopoverTrigger>open popover</PopoverTrigger>
          <PopoverContent>popover body</PopoverContent>
        </Popover>
      </>,
    )
    await userEvent.click(screen.getByText('open dialog'))
    expect(screen.getByRole('dialog', { name: 'Post' })).toBeInTheDocument()
    await userEvent.keyboard('{Escape}')
    await userEvent.click(screen.getByText('open popover'))
    expect(screen.getByText('popover body')).toBeInTheDocument()
  })

  it('select and dropdown render their triggers', () => {
    render(
      <>
        <Select defaultValue="1">
          <SelectTrigger aria-label="depot">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="1">Depot 1</SelectItem>
          </SelectContent>
        </Select>
        <DropdownMenu>
          <DropdownMenuTrigger>more</DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuItem>Cancel</DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </>,
    )
    expect(screen.getByRole('combobox', { name: 'depot' })).toHaveTextContent('Depot 1')
    expect(screen.getByRole('button', { name: 'more' })).toHaveAttribute('aria-haspopup', 'menu')
  })

  it('command filters items', async () => {
    render(
      <Command>
        <CommandInput placeholder="Search" />
        <CommandList>
          <CommandEmpty>none</CommandEmpty>
          <CommandItem>Filter 04.123</CommandItem>
          <CommandItem>Pump 77</CommandItem>
        </CommandList>
      </Command>,
    )
    await userEvent.type(screen.getByPlaceholderText('Search'), 'pump')
    expect(screen.getByText('Pump 77')).toBeInTheDocument()
    expect(screen.queryByText('Filter 04.123')).not.toBeInTheDocument()
  })

  it('toaster mounts', () => {
    render(<Toaster />)
    expect(document.querySelector('section[aria-label]')).not.toBeNull()
  })
})
